package br.com.sensimap.model;


/** Objeto imutável; novos registros têm ID nulo até a persistência. */
public final class LocalEstimulo {
    private final Local local;
    private final Estimulo estimulo;


    public LocalEstimulo(Local local, Estimulo estimulo) {
        if (local == null || local.getId() == null || estimulo == null || estimulo.getId() == null) {
            throw new IllegalArgumentException("Informe local e estímulo com ID persistido.");
        }
        this.local = local;
        this.estimulo = estimulo;
    }

    public Local getLocal() { return local; }
    public Estimulo getEstimulo() { return estimulo; }
}
