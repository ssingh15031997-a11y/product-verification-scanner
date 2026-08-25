package com.example

import com.example.data.model.Product
import com.example.data.remote.ProductApiResult
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductServiceTest {

    @Test
    fun parse_single_product_success_response() {
        val sampleJson = """
        {
          "success": true,
          "found": true,
          "duplicate": false,
          "count": 1,
          "products": [
            {
              "id": 1,
              "model": "Nova2Ultra",
              "color": "Black",
              "memory": "6+128",
              "ean": "8906202671265",
              "sku": "AINT68BLA5",
              "price": 29999,
              "sarValue": "Body - 1.280, Head - 1.397"
            }
          ]
        }
        """.trimIndent()

        val root = JSONObject(sampleJson)
        val success = root.optBoolean("success", true)
        val found = root.optBoolean("found", false)
        val duplicate = root.optBoolean("duplicate", false)
        val count = root.optInt("count", 0)

        assertTrue(success)
        assertTrue(found)
        assertFalse(duplicate)
        assertEquals(1, count)

        val productsArray = root.getJSONArray("products")
        val obj = productsArray.getJSONObject(0)
        val product = Product(
            id = obj.opt("id")?.toString() ?: "1",
            model = obj.optString("model"),
            color = obj.optString("color"),
            memory = obj.optString("memory"),
            ean = obj.optString("ean"),
            sku = obj.optString("sku"),
            price = obj.opt("price")?.toString() ?: "",
            sarValue = obj.optString("sarValue")
        )

        assertEquals("Nova2Ultra", product.model)
        assertEquals("Black", product.color)
        assertEquals("6+128", product.memory)
        assertEquals("8906202671265", product.ean)
        assertEquals("AINT68BLA5", product.sku)
        assertEquals("29999", product.price)
        assertEquals("Body - 1.280, Head - 1.397", product.sarValue)
    }

    @Test
    fun parse_not_found_response() {
        val sampleJson = """
        {
          "success": true,
          "found": false,
          "duplicate": false,
          "count": 0,
          "products": []
        }
        """.trimIndent()

        val root = JSONObject(sampleJson)
        val found = root.optBoolean("found", false)
        val duplicate = root.optBoolean("duplicate", false)
        val productsArray = root.getJSONArray("products")

        assertFalse(found)
        assertFalse(duplicate)
        assertEquals(0, productsArray.length())
    }

    @Test
    fun parse_duplicate_ean_response() {
        val sampleJson = """
        {
          "success": true,
          "found": true,
          "duplicate": true,
          "count": 2,
          "products": [
            {
              "id": 4,
              "model": "Nova2Ultra Pro",
              "color": "Titanium Silver",
              "memory": "8+256",
              "ean": "8906202671296",
              "sku": "AINT68SIL8",
              "price": 34999,
              "sarValue": "Body - 1.280, Head - 1.397"
            },
            {
              "id": 5,
              "model": "Nova2Ultra Pro (Special Edition)",
              "color": "Titanium Silver",
              "memory": "8+256",
              "ean": "8906202671296",
              "sku": "AINT68SIL8-SE",
              "price": 36999,
              "sarValue": "Body - 1.280, Head - 1.397"
            }
          ]
        }
        """.trimIndent()

        val root = JSONObject(sampleJson)
        val found = root.optBoolean("found", false)
        val duplicate = root.optBoolean("duplicate", false)
        val count = root.optInt("count", 0)

        assertTrue(found)
        assertTrue(duplicate)
        assertEquals(2, count)
    }

    @Test
    fun sequential_product_parsing_does_not_retain_previous_sar_value() {
        val product1Json = """
        {
          "success": true,
          "found": true,
          "duplicate": false,
          "products": [
            {
              "id": "1",
              "model": "Nova2Ultra",
              "color": "Black",
              "memory": "6+128",
              "ean": "8906202671265",
              "sku": "AINT68BLA5",
              "price": "29999",
              "sarValue": "Body - 1.280, Head - 1.397"
            }
          ]
        }
        """.trimIndent()

        val product2Json = """
        {
          "success": true,
          "found": true,
          "duplicate": false,
          "products": [
            {
              "id": "2",
              "model": "Evo (4G)",
              "color": "Blue",
              "memory": "4+64",
              "ean": "8906202671500",
              "sku": "EVO4GBLU4",
              "price": "14999",
              "sarValue": "Body - 0.760, Head - 0.840"
            }
          ]
        }
        """.trimIndent()

        // Parse product 1
        val root1 = JSONObject(product1Json)
        val obj1 = root1.getJSONArray("products").getJSONObject(0)
        val p1 = Product(
            id = obj1.getString("id"),
            model = obj1.getString("model"),
            color = obj1.getString("color"),
            memory = obj1.getString("memory"),
            ean = obj1.getString("ean"),
            sku = obj1.getString("sku"),
            price = obj1.getString("price"),
            sarValue = obj1.getString("sarValue")
        )

        assertEquals("Nova2Ultra", p1.model)
        assertEquals("Body - 1.280, Head - 1.397", p1.sarValue)

        // Parse product 2
        val root2 = JSONObject(product2Json)
        val obj2 = root2.getJSONArray("products").getJSONObject(0)
        val p2 = Product(
            id = obj2.getString("id"),
            model = obj2.getString("model"),
            color = obj2.getString("color"),
            memory = obj2.getString("memory"),
            ean = obj2.getString("ean"),
            sku = obj2.getString("sku"),
            price = obj2.getString("price"),
            sarValue = obj2.getString("sarValue")
        )

        assertEquals("Evo (4G)", p2.model)
        assertEquals("Blue", p2.color)
        assertEquals("4+64", p2.memory)
        assertEquals("8906202671500", p2.ean)
        assertEquals("EVO4GBLU4", p2.sku)
        assertEquals("14999", p2.price)
        assertEquals("Body - 0.760, Head - 0.840", p2.sarValue)
        // Ensure no stale value leaked from p1
        assertTrue(p2.sarValue != p1.sarValue)
    }

    @Test
    fun parse_product_with_empty_sar_value() {
        val json = """
        {
          "success": true,
          "found": true,
          "duplicate": false,
          "products": [
            {
              "id": "3",
              "model": "Budget Feature Phone",
              "color": "Grey",
              "memory": "32MB",
              "ean": "8906202679999",
              "sku": "BFP01GRY",
              "price": "1999",
              "sarValue": ""
            }
          ]
        }
        """.trimIndent()

        val root = JSONObject(json)
        val obj = root.getJSONArray("products").getJSONObject(0)
        val product = Product(
            id = obj.getString("id"),
            model = obj.getString("model"),
            color = obj.getString("color"),
            memory = obj.getString("memory"),
            ean = obj.getString("ean"),
            sku = obj.getString("sku"),
            price = obj.getString("price"),
            sarValue = obj.optString("sarValue", "")
        )

        assertEquals("", product.sarValue)
    }
}
