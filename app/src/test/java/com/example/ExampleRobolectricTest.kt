package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.repository.AuthRepository
import com.example.util.EanValidator
import com.example.util.PriceFormatter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authRepository = AuthRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun app_name_is_correct() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Product Verification Scanner", appName)
    }

    @Test
    fun priceFormatter_formats_correctly() {
        assertEquals("₹29,999", PriceFormatter.format("29999"))
        assertEquals("₹29,999", PriceFormatter.format("29,999"))
        assertEquals("₹34,999", PriceFormatter.format("₹34,999"))
        assertEquals("₹0", PriceFormatter.format(""))
    }

    @Test
    fun eanValidator_cleans_and_validates() {
        assertEquals("8906202671265", EanValidator.cleanEan("  8906202671265  "))
        assertEquals("8906202671265", EanValidator.cleanEan("890 620 267 1265"))

        assertTrue(EanValidator.validateManualInput("8906202671265") is EanValidator.ValidationResult.Valid)
        assertTrue(EanValidator.validateManualInput("12345") is EanValidator.ValidationResult.Error)
        assertTrue(EanValidator.validateManualInput("890ABC1265") is EanValidator.ValidationResult.Error)
    }

    @Test
    fun authRepository_verifies_operator_credentials() {
        assertTrue(authRepository.login("sanjay2007", "Sanjay@2007"))
        assertTrue(authRepository.currentUser.value.isLoggedIn)
        assertEquals("sanjay2007", authRepository.currentUser.value.userId)

        authRepository.logout()
        assertFalse(authRepository.currentUser.value.isLoggedIn)

        assertFalse(authRepository.login("sanjay2007", "WrongPass"))
        assertFalse(authRepository.login("wrongUser", "Sanjay@2007"))
    }

    @Test
    fun productDao_persists_and_queries_ean() = runBlocking {
        val product1 = ProductEntity(
            sheetId = "1",
            model = "Nova2Ultra",
            color = "Black",
            memory = "6+128",
            ean = "8906202671265",
            sku = "AINT68BLA5",
            price = "29999",
            sarValue = "Body - 1.280, Head - 1.397"
        )
        val product2 = ProductEntity(
            sheetId = "2",
            model = "Evo (4G)",
            color = "Blue",
            memory = "4+64",
            ean = "8906202671500",
            sku = "EVO4GBLU4",
            price = "14999",
            sarValue = "Body - 0.760, Head - 0.840"
        )

        database.productDao().insertAll(listOf(product1, product2))

        val found1 = database.productDao().findProductsByEan("8906202671265")
        assertEquals(1, found1.size)
        assertEquals("Nova2Ultra", found1.first().model)
        assertEquals("Body - 1.280, Head - 1.397", found1.first().sarValue)

        // Query product 2 sequentially
        val found2 = database.productDao().findProductsByEan("8906202671500")
        assertEquals(1, found2.size)
        assertEquals("Evo (4G)", found2.first().model)
        assertEquals("Body - 0.760, Head - 0.840", found2.first().sarValue)

        val notFound = database.productDao().findProductsByEan("9999999999999")
        assertTrue(notFound.isEmpty())
    }

    @Test
    fun productDao_detects_duplicates() = runBlocking {
        val dup1 = ProductEntity(
            sheetId = "4",
            model = "Nova2Ultra Pro",
            color = "Titanium Silver",
            memory = "8+256",
            ean = "8906202671296",
            sku = "AINT68SIL8",
            price = "34999",
            sarValue = "Body - 1.280, Head - 1.397"
        )
        val dup2 = ProductEntity(
            sheetId = "5",
            model = "Nova2Ultra Pro (Special Edition)",
            color = "Titanium Silver",
            memory = "8+256",
            ean = "8906202671296",
            sku = "AINT68SIL8-SE",
            price = "36999",
            sarValue = "Body - 1.280, Head - 1.397"
        )

        database.productDao().insertAll(listOf(dup1, dup2))

        val duplicates = database.productDao().findProductsByEan("8906202671296")
        assertEquals(2, duplicates.size)
    }
}

