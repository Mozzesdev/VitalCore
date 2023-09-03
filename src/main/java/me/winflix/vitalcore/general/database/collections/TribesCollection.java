package me.winflix.vitalcore.general.database.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.entity.Player;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.tribe.files.TribeFile;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.TribeMember;
import me.winflix.vitalcore.tribe.utils.RankManager;

public class TribesCollection {

    public static MongoCollection<Tribe> tribesCollection;

    public static void initialize(MongoDatabase database) {
        PojoCodecProvider pojoAutoProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoAutoProvider));

        tribesCollection = database.getCollection("Tribes", Tribe.class).withCodecRegistry(pojoCodecRegistry);
    }

    public static Tribe createTribe(Player player) {
        String playerName = player.getDisplayName();
        UUID playerUUID = player.getUniqueId();

        TribeMember owner = new TribeMember(playerName, playerUUID.toString());
        owner.setRange(RankManager.OWNER_RANK);

        String tribeName = "Tribe_of_" + owner.getPlayerName();
        String tribeId = UUID.randomUUID().toString();

        Tribe tribeModel = new Tribe(tribeName, tribeId);
        tribeModel.addMember(owner);

        TribeFile tribeFile = new TribeFile(tribeId, "tribes", tribeModel);
        VitalCore.fileManager.getTribesFiles().add(tribeFile);

        tribesCollection.insertOne(tribeModel);

        return tribeModel;
    }

    public static Tribe getTribeById(String id) {
        return tribesCollection.find(Filters.eq("_id", id)).first();
    }

    public static Tribe getTribeByName(String name) {
        return tribesCollection.find(Filters.eq("tribeName", name)).first();
    }

    public static Tribe saveTribe(Tribe tribe) {
        // Eliminar el archivo YAML antiguo solo si existe
        TribeFile oldFile = VitalCore.fileManager.getTribeFile(tribe.getId());
        if (oldFile.getFile().exists()) {
            oldFile.getFile().delete();
            VitalCore.fileManager.getTribesFiles().remove(oldFile);
        }

        // Crear un nuevo archivo YAML y agregarlo a la lista de archivos
        TribeFile newFile = new TribeFile(tribe.getId(), "tribes", tribe);
        VitalCore.fileManager.getTribesFiles().add(newFile);

        // Reemplazar el documento en la colección
        tribesCollection.findOneAndReplace(Filters.eq("_id", tribe.getId()), tribe);

        return tribe;
    }

    public static void deleteTribe(Tribe tribe) {
        try {
            TribeFile tribeFile = VitalCore.fileManager.getTribeFile(tribe.getId());
            File yamlFile = tribeFile.getFile();

            if (yamlFile.exists() && yamlFile.delete()) {
                System.out.println("Archivo de tribu eliminado: " + yamlFile.getPath());
            } else {
                System.out.println("No se pudo eliminar el archivo de tribu: " + yamlFile.getPath());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        tribesCollection.deleteOne(Filters.eq("_id", tribe.getId()));
    }

    public static ArrayList<Tribe> getAllTribes() {
        return tribesCollection.find().into(new ArrayList<Tribe>());
    }

}
