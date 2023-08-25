package me.winflix.vitalcore.interfaces;

import java.util.List;

public class ConfirmMessages {
    String confirm;
    List<String> confirmLore;
    String cancel;
    List<String> cancelLore;

    public ConfirmMessages(String confirm, List<String> confirmLore, String cancel, List<String> cancelLore) {
        this.confirm = confirm;
        this.confirmLore = confirmLore;
        this.cancel = cancel;
        this.cancelLore = cancelLore;
    }

    public String getConfirm() {
        return confirm;
    }

    public String getCancel() {
        return cancel;
    }

    public List<String> getCancelLore() {
        return cancelLore;
    }

    public List<String> getConfirmLore() {
        return confirmLore;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public void setCancelLore(List<String> cancelLore) {
        this.cancelLore = cancelLore;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public void setConfirmLore(List<String> confirmLore) {
        this.confirmLore = confirmLore;
    }
}
