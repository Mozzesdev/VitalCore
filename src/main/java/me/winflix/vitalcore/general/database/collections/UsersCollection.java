package me.winflix.vitalcore.general.database.collections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.tribe.files.UserFile;
import me.winflix.vitalcore.tribe.files.TribeFile;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.User;

public class UsersCollection {

    public static MongoCollection<User> userCollection;
    public static VitalCore plugin = VitalCore.getPlugin();

    public static void initialize(MongoDatabase database) {
        PojoCodecProvider pojoAutoProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoAutoProvider));
        userCollection = database.getCollection("Users", User.class).withCodecRegistry(pojoCodecRegistry);
    }

    public static User getUser(UUID uuid) {
        return userCollection.find(Filters.eq("_id", uuid.toString())).first();
    }

    public static User createUser(Player player, UUID uuid, Tribe tribe) {
        String playerId = uuid.toString();
        String playerDisplayName = player.getDisplayName();

        User playerModel = new User(playerDisplayName, playerId, tribe.getId());
        UserFile playerFile = new UserFile(plugin, playerId, "users", playerModel);

        VitalCore.fileManager.getUsersFiles().add(playerFile); // Agregar a la lista de archivos

        // Insertar en la colección de jugadores
        userCollection.insertOne(playerModel);

        return playerModel;
    }

    public static User saveUser(User p) {
        // Eliminar el archivo YAML antiguo solo si existe
        UserFile oldFile = VitalCore.fileManager.getUserFile(p.getId());
        p.setTribe(null);
        if (oldFile.getFile().exists()) {
            oldFile.getFile().delete();
            VitalCore.fileManager.getUsersFiles().remove(oldFile);
        }

        // Crear un nuevo archivo YAML y agregarlo a la lista de archivos
        UserFile newFile = new UserFile(plugin, p.getId(), "users", p);
        VitalCore.fileManager.getUsersFiles().add(newFile);

        // Reemplazar el documento en la colección
        userCollection.findOneAndReplace(Filters.eq("_id", p.getId()), p);

        return p;
    }

    public static User getUserWithTribe(UUID playerId) {
        UserFile playerFile = VitalCore.fileManager.getUserFile(playerId.toString());

        if (playerFile == null) {
            User userDB = getUser(playerId);

            if (userDB == null) {
                return null;
            }

            playerFile = new UserFile(plugin, userDB.getId(), "users", userDB);
            VitalCore.fileManager.getUsersFiles().add(playerFile);
        }

        File playerYamlFile = new File(playerFile.getPath());

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            User player = mapper.readValue(playerYamlFile, User.class);

            if (player != null && player.getTribeId() != null) {
                TribeFile tribeFile = VitalCore.fileManager.getTribeFile(player.getTribeId());
                File tribeYamlFile = new File(tribeFile.getPath());

                if (tribeYamlFile.exists()) {
                    Tribe tribe = mapper.readValue(tribeYamlFile, Tribe.class);
                    player.setTribe(tribe);
                }
            }

            return player;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean syncDBWithFiles() {
        try {
            VitalCore.fileManager.setupUsersFiles();
            VitalCore.fileManager.setupTribesFiles();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static ArrayList<User> getAllUsers() {
        return userCollection.find().into(new ArrayList<User>());
    }

}
