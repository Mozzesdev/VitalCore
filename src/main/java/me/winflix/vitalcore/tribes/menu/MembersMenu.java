package me.winflix.vitalcore.tribes.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.winflix.vitalcore.general.database.collections.tribe.TribesDAO;
import me.winflix.vitalcore.general.menu.PaginatedMenu;
import me.winflix.vitalcore.general.utils.PlayerUtils;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.tribes.models.Tribe;
import me.winflix.vitalcore.tribes.models.TribeMember;

public class MembersMenu extends PaginatedMenu {

    private final Tribe tribe;
    private final List<ItemStack> memberItems = new ArrayList<>();

    public MembersMenu(Player owner) {
        super(owner);
        this.tribe = TribesDAO.getTribeByMember(owner.getUniqueId());
        loadMemberItems();
    }

    private void loadMemberItems() {
        memberItems.clear();
        for (TribeMember member : tribe.getMembers()) {
            ItemStack skull = createMemberSkull(member);
            memberItems.add(skull);
        }
    }

    private ItemStack createMemberSkull(TribeMember member) {
        List<String> lore = new ArrayList<>();
        lore.add(Utils.useColors("&7Name: &a" + member.getPlayerName()));
        lore.add(Utils.useColors("&7Range: &a" + member.getRange().getTag()));
        
        ItemStack skull = PlayerUtils.getPlayerSkull(
            member.getPlayerName(),
            member.getPlayerId(),
            lore.toArray(new String[0])
        );
        
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(Utils.useColors("&e" + member.getPlayerName()));
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    protected List<ItemStack> getAllItems() {
        return memberItems;
    }

    @Override
    public String getMenuName() {
        return Utils.useColors("&0Miembros de " + tribe.getTribeName());
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        super.handleMenu(event); // Manejo de paginación
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Manejar clicks en items de miembros
        if (clickedItem.getType() == Material.PLAYER_HEAD && 
            clickedItem.getItemMeta() instanceof SkullMeta skullMeta &&
            skullMeta.getOwningPlayer() != null) {
            
            UUID memberUUID = skullMeta.getOwningPlayer().getUniqueId();
            TribeMember member = tribe.getMember(memberUUID);
            
            if (member != null) {
                event.getWhoClicked().sendMessage(
                    Utils.useColors("&aRango de " + member.getPlayerName() + ": " + member.getRange().getName())
                );
            }
        }
    }
}
