import { ApiResponse, Product } from '../types';

export const DEFAULT_API_ENDPOINT =
  'https://script.google.com/macros/s/AKfycbwIh8nemekW3RTdVhxyocrIrd8jjKRdjQXIGM5iQs4guuUYu8g4nxKrokg8f_UA8molnA/exec';

export interface ProductLookupResult {
  success: boolean;
  found: boolean;
  duplicate: boolean;
  count: number;
  message?: string;
  products: Product[];
  error?: string;
}

/**
 * Validates whether the given string is a valid numeric EAN barcode.
 */
export function validateEan(ean: string): { valid: boolean; cleaned: string; error?: string } {
  const cleaned = (ean || '').trim().replace(/[\s-]/g, '');
  if (!cleaned) {
    return { valid: false, cleaned: '', error: 'EAN barcode cannot be empty' };
  }
  if (!/^\d+$/.test(cleaned)) {
    return { valid: false, cleaned, error: 'EAN must contain numbers only' };
  }
  if (cleaned.length < 4 || cleaned.length > 18) {
    return { valid: false, cleaned, error: `Invalid EAN length (${cleaned.length} digits). Standard is 8, 12, 13, or 14 digits.` };
  }
  return { valid: true, cleaned };
}

/**
 * Parses raw product item from Apps Script JSON into a clean, strongly-typed Product object.
 * Every product object is created freshly from the JSON response.
 * SAR value is extracted strictly from the current response without any static fallbacks.
 */
function parseProductFromJson(item: Record<string, unknown>, fallbackEan: string): Product {
  const id = item.id !== undefined && item.id !== null ? String(item.id).trim() : '1';
  const model = item.model !== undefined && item.model !== null ? String(item.model).trim() : '';
  const color = item.color !== undefined && item.color !== null ? String(item.color).trim() : '';
  const memory = item.memory !== undefined && item.memory !== null ? String(item.memory).trim() : '';
  const productEan = item.ean !== undefined && item.ean !== null && String(item.ean).trim().length > 0
    ? String(item.ean).trim()
    : fallbackEan;
  const sku = item.sku !== undefined && item.sku !== null ? String(item.sku).trim() : '';
  const price = item.price !== undefined && item.price !== null ? String(item.price).trim() : '';

  // Extract SAR value strictly from the current item's payload
  let sarValue = '';
  if (item.sarValue !== undefined && item.sarValue !== null) {
    sarValue = String(item.sarValue).trim();
  } else if (item.sar_value !== undefined && item.sar_value !== null) {
    sarValue = String(item.sar_value).trim();
  } else if (item['SAR Value'] !== undefined && item['SAR Value'] !== null) {
    sarValue = String(item['SAR Value']).trim();
  } else if (item.sar !== undefined && item.sar !== null) {
    sarValue = String(item.sar).trim();
  }

  // Extract Carton MRP directly from the API response payload
  let cartonMrp: string | number | undefined | null = undefined;
  if (item.cartonMrp !== undefined && item.cartonMrp !== null) {
    cartonMrp = item.cartonMrp as string | number;
  } else if (item.carton_mrp !== undefined && item.carton_mrp !== null) {
    cartonMrp = item.carton_mrp as string | number;
  } else if (item['Carton MRP'] !== undefined && item['Carton MRP'] !== null) {
    cartonMrp = item['Carton MRP'] as string | number;
  }

  return {
    id,
    model,
    color,
    memory,
    ean: productEan,
    sku,
    price,
    sarValue, // Brand-new value directly from this API item
    cartonMrp,
  };
}

/**
 * Looks up product details from the Google Apps Script Web App API.
 * The Apps Script API is the single source of truth.
 *
 * @param ean Raw EAN barcode
 * @param endpoint Optional custom Apps Script Web App endpoint URL
 * @param signal Optional AbortSignal to cancel in-flight queries
 */
export async function getProductByEAN(
  ean: string,
  endpoint: string = DEFAULT_API_ENDPOINT,
  signal?: AbortSignal
): Promise<ProductLookupResult> {
  const validation = validateEan(ean);
  if (!validation.valid) {
    return {
      success: false,
      found: false,
      duplicate: false,
      count: 0,
      products: [],
      error: validation.error || 'Invalid EAN barcode',
    };
  }

  const cleanEan = validation.cleaned;
  const baseUrl = (endpoint || '').trim() || DEFAULT_API_ENDPOINT;

  // Build target URL
  const separator = baseUrl.includes('?') ? '&' : '?';
  const url = `${baseUrl}${separator}ean=${encodeURIComponent(cleanEan)}`;

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
      signal,
    });

    if (!response.ok) {
      return {
        success: false,
        found: false,
        duplicate: false,
        count: 0,
        products: [],
        error: `API Server returned HTTP ${response.status} (${response.statusText})`,
      };
    }

    const text = await response.text();
    if (!text || !text.trim()) {
      return {
        success: false,
        found: false,
        duplicate: false,
        count: 0,
        products: [],
        error: 'Received empty response from Apps Script API',
      };
    }

    let json: ApiResponse;
    try {
      json = JSON.parse(text);
    } catch {
      return {
        success: false,
        found: false,
        duplicate: false,
        count: 0,
        products: [],
        error: 'Failed to parse JSON response from Apps Script API',
      };
    }

    const found = Boolean(json.found);
    const duplicate = Boolean(json.duplicate);
    const rawProducts = Array.isArray(json.products) ? json.products : [];

    // Construct completely new Product instances for every item
    const products: Product[] = rawProducts.map((p) =>
      parseProductFromJson(p as unknown as Record<string, unknown>, cleanEan)
    );

    return {
      success: Boolean(json.success),
      found,
      duplicate,
      count: json.count || products.length,
      message: json.message,
      products,
    };
  } catch (err: unknown) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      throw err; // Let caller know it was explicitly aborted for a newer scan
    }
    const message = err instanceof Error ? err.message : 'Unknown network error';
    return {
      success: false,
      found: false,
      duplicate: false,
      count: 0,
      products: [],
      error: `Connection error: ${message}`,
    };
  }
}
