package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import com.biblioteca.web.ErroresController;

class ErroresArchivoTest {
    @Test void cerrarLectorNoIntentaRenderizarErrorHtml() {
        assertNull(new ErroresController().archivoInterrumpido(new IOException("Broken pipe"),new MockHttpServletResponse()));
    }
    @Test void respuestaYaEnviadaNoSeReescribe() throws IOException {
        var respuesta=new MockHttpServletResponse();respuesta.getOutputStream().write(1);respuesta.flushBuffer();
        assertNull(new ErroresController().archivoInterrumpido(new IOException("Archivo interrumpido"),respuesta));
        assertEquals(1,respuesta.getContentAsByteArray().length);
    }
    @Test void falloAntesDeEnviarLimpiaLosBytesYDevuelveErrorLegible() throws IOException {
        var respuesta=new MockHttpServletResponse();respuesta.getOutputStream().write(1);
        var vista=new ErroresController().archivoInterrumpido(new IOException("Archivo inaccesible"),respuesta);
        assertEquals("error-app",vista.getViewName());assertEquals(400,respuesta.getStatus());
        assertEquals(0,respuesta.getContentAsByteArray().length);assertDoesNotThrow(()->respuesta.getWriter());
    }
}
