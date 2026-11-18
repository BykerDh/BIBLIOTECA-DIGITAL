package com.biblioteca.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="app.vencimientos.habilitados",havingValue="true",matchIfMissing=true)
public class ProgramadorVencimientos {
    private final VencimientoService vencimientos;
    public ProgramadorVencimientos(VencimientoService vencimientos) { this.vencimientos = vencimientos; }
    @Scheduled(fixedDelay=60000,initialDelay=10000)
    public void revisar() { vencimientos.procesar(); }
}
