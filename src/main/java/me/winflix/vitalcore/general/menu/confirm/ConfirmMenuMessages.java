package me.winflix.vitalcore.general.menu.confirm;

import java.util.List;

public abstract interface ConfirmMenuMessages {

    String getConfirmMessages();

    List<String> getConfirmLore();

    String getDeniedMessages();

    List<String> getDeniedLore();
}