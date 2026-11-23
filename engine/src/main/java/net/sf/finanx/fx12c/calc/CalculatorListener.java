package net.sf.finanx.fx12c.calc;

/**
 * Notifica a UI de eventos assíncronos do motor financeiro.
 *
 * <p>Esta interface desacopla o motor (Java puro) de qualquer toolkit gráfico.
 * A camada Android (ViewModel) registra um listener e reage a:</p>
 * <ul>
 *   <li>{@link #onDisplayUpdate()} — o conteúdo do display mudou e precisa ser
 *       redesenhado. Equivalente ao antigo {@code controller.getWindow().updateDisplay()}
 *       do Finanx 12c original (acoplado ao Swing).</li>
 *   <li>{@link #onQuit()} — o usuário pressionou ON+ON (sequência dupla da tecla
 *       ON) solicitando o "desligamento" da calculadora. Em Android, isso
 *       tipicamente resulta em {@code Activity.finish()}.</li>
 * </ul>
 *
 * <p>Licença: MIT (mantida do projeto Finanx 12c original, Copyright (c)
 * 2007-2020 Fabio Lima). Esta adição é parte da refatoração Android.</p>
 */
public interface CalculatorListener {

    /** Chamado toda vez que o display principal deve ser redesenhado. */
    void onDisplayUpdate();

    /** Chamado quando a calculadora solicita encerramento (ON pressionado 2x). */
    void onQuit();
}
