package com.pruebasUnitarias.controlador;

import com.controlador.ControladorLogin;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

public class ControladorLoginTest {

    @InjectMocks
    private ControladorLogin controladorLogin;

    @Mock
    private UsuarioPersonalDAO mockUsuarioDAO;

    private TextField txtUsuarioSimulado;
    private PasswordField txtPasswordSimulado;

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // El hilo ya estaba corriendo, ignoramos el error
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        controladorLogin = new ControladorLogin();
        MockitoAnnotations.openMocks(this);

        // Instanciamos controles JavaFX reales de forma manual
        txtUsuarioSimulado = new TextField();
        txtPasswordSimulado = new PasswordField();
        // Usamos reflexión para inyectar estos campos simulados en las propiedades privadas @FXML del controlador
        inyectarCampoPrivado("txtUsuario", txtUsuarioSimulado);
        inyectarCampoPrivado("txtPassword", txtPasswordSimulado);
        inyectarCampoPrivado("usuarioDAO", mockUsuarioDAO);
    }

    /**
     * Utilidad por reflexión para mapear los campos privados @FXML de tu controlador
     */
    private void inyectarCampoPrivado(String nombreCampo, Object valor) throws Exception {
        Field field = ControladorLogin.class.getDeclaredField(nombreCampo);
        field.setAccessible(true);
        field.set(controladorLogin, valor);
    }

    /**
     * Utilidad para invocar el método privado 'validarCredenciales()' del controlador
     */
    private boolean invocarValidarCredenciales() throws Exception {
        Method method = ControladorLogin.class.getDeclaredMethod("validarCredenciales");
        method.setAccessible(true);

        // Usamos un CompletableFuture para transferir la ejecución al hilo de JavaFX y capturar el resultado booleano
        CompletableFuture<Boolean> resultadoFuturo = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                boolean res = (boolean) method.invoke(controladorLogin);
                resultadoFuturo.complete(res);
            } catch (Exception e) {
                resultadoFuturo.completeExceptionally(e);
            }
        });

        // Espera de forma síncrona a que el hilo de JavaFX procese el método y retorne el valor
        return resultadoFuturo.get();
    }
    // ============================================================
    //  CASOS DE PRUEBA
    // ============================================================

    @Test
    @DisplayName("validarCredenciales debería retornar false si los campos están completamente vacíos")
    public void testValidarCredencialesCamposVacios() throws Exception {
        // 1. Arreglar: Dejar los inputs vacíos
        txtUsuarioSimulado.setText("");
        txtPasswordSimulado.setText("");

        // 2. Actuar e Invocamos indirectamente por reflexión
        boolean resultado = invocarValidarCredenciales();

        // 3. Afirmar
        assertFalse(resultado, "No debería validar credenciales si el email o contraseña están vacíos");
        // Aseguramos que el DAO jamás haya sido consultado
        verifyNoInteractions(mockUsuarioDAO);
    }

    @Test
    @DisplayName("validarCredenciales debería retornar true si el DAO autentica exitosamente al usuario")
    public void testValidarCredencialesExitoso() throws Exception {
        // 1. Arreglar: Seteamos datos válidos
        txtUsuarioSimulado.setText("operador@ferreteria.com");
        txtPasswordSimulado.setText("clave123");

        UsuarioPersonal usuarioMock = new UsuarioPersonal();
        usuarioMock.setIdUsuario(1);
        usuarioMock.setNombre("Alvaro");

        // Simulamos el login positivo en el DAO
        when(mockUsuarioDAO.autenticarUsuario("operador@ferreteria.com", "clave123")).thenReturn(usuarioMock);

        // 2. Actuar
        boolean resultado = invocarValidarCredenciales();

        // 3. Afirmar
        assertTrue(resultado, "Debería retornar true si las credenciales son válidas y registradas");
        verify(mockUsuarioDAO, times(1)).autenticarUsuario("operador@ferreteria.com", "clave123");
    }

    @Test
    @DisplayName("validarCredenciales debería retornar false (Acceso Denegado) ante credenciales inválidas")
    public void testValidarCredencialesIncorrectas() throws Exception {
        // 1. Arreglar: Credenciales erróneas
        txtUsuarioSimulado.setText("usuario@falso.com");
        txtPasswordSimulado.setText("erronea");

        // El DAO retornará nulo indicando login fallido
        when(mockUsuarioDAO.autenticarUsuario("usuario@falso.com", "erronea")).thenReturn(null);

        // 2. Actuar
        boolean resultado = invocarValidarCredenciales();

        // 3. Afirmar
        assertFalse(resultado, "Debería retornar false si el email o password no coinciden");
        verify(mockUsuarioDAO, times(1)).autenticarUsuario("usuario@falso.com", "erronea");
    }
}