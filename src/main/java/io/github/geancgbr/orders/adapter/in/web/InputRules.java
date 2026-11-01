package io.github.geancgbr.orders.adapter.in.web;

final class InputRules {

    static final String FREE_TEXT = "^[\\p{L}\\p{N} .,:;!?'\"()/+&%#-]+$";
    static final String FREE_TEXT_MESSAGE = "contém caracteres não permitidos";
    static final String CODE = "^[A-Za-z0-9_-]+$";
    static final String CODE_MESSAGE = "aceita só letras, números, hífen e sublinhado";

    private InputRules() {}
}
