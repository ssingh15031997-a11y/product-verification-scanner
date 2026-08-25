# Product Verification Scanner (Web Edition)

Industrial real-time EAN barcode verification system powered by Google Apps Script Web App API as the single source of truth. Built for instant browser deployment across multiple workstations and mobile devices.

## Multi-Platform Workstation Deployment
This web application is engineered to run from **ONE universal URL** on:
1. **Windows Desktop 1** (Packaging Line)
2. **Windows Desktop 2** (Quality Audit Bay)
3. **Windows Desktop 3** (Final Assembly)
4. **Windows Desktop 4** (Inbound Receiving)
5. **Android Mobile / Tablet** (Camera Barcode Scanning & PWA)

---

## Key Features

- **Live Google Apps Script Master Database**: Queries the Apps Script Web App executable API in real-time (`GET ?ean=<EAN>`). Zero direct Google Sheets exposure or client-side secret keys.
- **Hardware Barcode Scanner Support**: Automatic keystroke burst detection for USB and Bluetooth handheld barcode scanners (Wedge mode: EAN + Enter) on Windows workstations.
- **Integrated Mobile Camera Viewfinder**: High-speed camera barcode scanning with torch flashlight toggle and targeting reticle using `html5-qrcode`.
- **Zero-Stale SAR State Engine**: Every scan creates a brand new product object directly from the current API payload. Previous SAR values are discarded immediately prior to each lookup, preventing cross-model stale value retention.
- **Conflict & Error Resolution**:
  - **Single Product Match**: Full product specs, verified badge, SAR values, price, and operator QC checklist.
  - **Product Not Found**: Prominent alert for unregistered barcodes.
  - **Duplicate EAN Detected**: Disambiguation interface displaying conflicting SKUs for operator selection.
- **Industrial Sound Feedback**: Web Audio API POS synthesizer providing distinct audible cues for Verified OK, Duplicate Warning, and Error tones.
- **Session Scan Logs**: Local history of verified scans with export and inspection capabilities.

---

## Deployment to Vercel

### Option 1: Automatic Vercel GitHub Deployment
1. Push this repository to GitHub.
2. In the [Vercel Dashboard](https://vercel.com/new), select **Import Git Repository**.
3. Framework Preset will be auto-detected as **Vite**.
4. Root Directory: `./` (leave default).
5. Build Command: `npm run build`
6. Output Directory: `dist`
7. Click **Deploy**.

### Option 2: Vercel CLI
```bash
npm install -g vercel
vercel
```

---

## Local Development & Build Commands

```bash
# Install dependencies
npm install

# Start local development server (host 0.0.0.0, port 3000)
npm run dev

# Run production build
npm run build

# Preview production build
npm run preview
```
