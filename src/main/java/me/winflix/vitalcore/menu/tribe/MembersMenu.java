package me.winflix.vitalcore.menu.tribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.menu.PaginatedMenu;
import me.winflix.vitalcore.menu.PlayerMenuUtility;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.Utils;
import me.winflix.vitalcore.utils.PlayerUtils;

public class MembersMenu extends PaginatedMenu {

    public List<TribeMember> membersDB = null;
    public List<TribeMember> memberDataList = new ArrayList<>();
    int tribeSize = 0;

    public MembersMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        UUID inventoryOwnerId = playerMenuUtility.getOwner().getUniqueId();
        PlayerModel player = UserCollection.getPlayerWithTribe(inventoryOwnerId);
        TribeModel tribeOfOwner = player.getTribe(); 
        membersDB = tribeOfOwner.getMembers();
        tribeSize = membersDB.size();
    }

    @Override
    public String getMenuName() {
        return "Members";
    }

    @Override
    public int getSlots() {
        return Math.min(tribeSize <= 14 ? 36 : 45, 52);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem != null && membersDB != null) {
            for (TribeMember member : membersDB) {
                UUID memberUUID = UUID.fromString(member.getId());

                if (clickedItem.getItemMeta() instanceof SkullMeta &&
                        ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer() != null &&
                        ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer().getUniqueId().equals(memberUUID)) {

                    e.getWhoClicked().sendMessage(member.getRange());
                    break;
                }
            }
        }
    }

    @Deprecated
    @Override
    public void setMenuItems() {
        super.addMenuBorder();

        if (membersDB != null) {
            for (TribeMember member : membersDB) {

                List<String> memberLore = new ArrayList<>();
                memberLore.add(Utils.useColors("&7Name: &a" + member.getPlayerName()));
                memberLore.add(Utils.useColors("&7Range: &a" + member.getRange()));

                ItemStack playerSkull = PlayerUtils.getPlayerSkull(member.getPlayerName(),
                        UUID.fromString(member.getId()), memberLore.toArray(new String[0]));
                ItemMeta skullMeta = playerSkull.getItemMeta();

                skullMeta.setDisplayName(Utils.useColors("&e" + skullMeta.getDisplayName()));
                playerSkull.setItemMeta(skullMeta);

                inventory.addItem(playerSkull);
            }
        }
    }

}
