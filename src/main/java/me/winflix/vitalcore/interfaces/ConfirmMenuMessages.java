package me.winflix.vitalcore.interfaces;

import java.util.List;

public abstract interface ConfirmMenuMessages {

    String getConfirmMessages();

    List<String> getConfirmLore();

    String getDeniedMessages();

    List<String> getDeniedLore();
}