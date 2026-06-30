package com.example.carcare

import com.example.carcare.data.AuthSession
import com.example.carcare.data.repository.CrudRepository
import com.example.carcare.model.Identifiable
import com.example.carcare.model.Role
import com.example.carcare.ui.viewmodel.BaseListViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Lógica compartida de los ViewModels de lista (carga acotada por sesión + CRUD optimista).
 * Usa un repo falso + un VM de prueba sobre la clase base real.
 */
class BaseListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private data class TestItem(override val id: String, val name: String = "") : Identifiable

    private class FakeRepo : CrudRepository<TestItem> {
        val stored = mutableListOf<TestItem>()
        var fail = false
        override suspend fun getAll(): List<TestItem> { if (fail) error("boom"); return stored.toList() }
        override suspend fun create(item: TestItem): TestItem {
            val saved = item.copy(id = "real"); stored.add(saved); return saved
        }
        override suspend fun delete(id: String) { if (fail) error("boom"); stored.removeAll { it.id == id } }
    }

    private class TestVM(repo: FakeRepo) : BaseListViewModel<TestItem, FakeRepo>(repo, "TestVM") {
        fun crear(item: TestItem) = create(item)
        fun borrar(id: String) = optimisticDelete(id)
    }

    private fun login() = AuthSession.restore("token", Role.ADMIN, "Admin", "ced", null, false)

    @After fun limpiar() = AuthSession.clear()

    @Test
    fun load_sinSesion_noLlamaALaApi() {
        AuthSession.clear()
        val repo = FakeRepo().apply { stored.add(TestItem("1")) }
        val vm = TestVM(repo) // el init { load() } se dispara, pero sin sesión no carga
        assertTrue("sin sesión la lista queda vacía", vm.items.isEmpty())
    }

    @Test
    fun load_conSesion_traeLosItems() {
        login()
        val repo = FakeRepo().apply { stored.add(TestItem("1", "uno")) }
        val vm = TestVM(repo)
        assertEquals(1, vm.items.size)
    }

    @Test
    fun create_agregaElItemDevueltoPorElBackend() {
        login()
        val vm = TestVM(FakeRepo())
        vm.crear(TestItem("tmp", "nuevo"))
        assertEquals(1, vm.items.size)
        assertEquals("real", vm.items.first().id) // id real del backend, no el temporal
    }

    @Test
    fun optimisticDelete_revierteSiLaRedFalla() {
        login()
        val repo = FakeRepo().apply { stored.add(TestItem("1")) }
        val vm = TestVM(repo)
        assertEquals(1, vm.items.size)

        repo.fail = true
        vm.borrar("1") // lo saca de inmediato; al fallar la red, lo reinserta

        assertEquals("el borrado optimista debe revertirse al fallar", 1, vm.items.size)
    }
}
