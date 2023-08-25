package me.winflix.vitalcore.database.collections;

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
import me.winflix.vitalcore.files.PlayerFile;
import me.winflix.vitalcore.files.TribeFile;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeModel;

public class UserCollection {

    public static MongoCollection<PlayerModel> userCollection;

    public static void initialize(MongoDatabase database) {
        PojoCodecProvider pojoAutoProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoAutoProvider));

        userCollection = database.getCollection("Users", PlayerModel.class).withCodecRegistry(pojoCodecRegistry);
    }

    public static PlayerModel getPlayer(UUID uuid) {
        return userCollection.find(Filters.eq("_id", uuid.toString())).first();
    }

    public static PlayerModel createPlayer(Player player, UUID uuid, TribeModel tribe) {
        String playerId = uuid.toString();
        String playerDisplayName = player.getDisplayName();
        String playerFileName = playerId + ".yml";

        PlayerModel playerModel = new PlayerModel(playerDisplayName, playerId, tribe.getId());
        PlayerFile playerFile = new PlayerFile(VitalCore.getPlugin(), playerFileName, "users", playerModel);

        VitalCore.fileManager.getPlayersFiles().add(playerFile); // Agregar a la lista de archivos

        // Insertar en la colección de jugadores
        userCollection.insertOne(playerModel);

        return playerModel;
    }

    public static PlayerModel savePlayer(PlayerModel p) {
        // Eliminar el archivo YAML antiguo solo si existe
        PlayerFile oldFile = VitalCore.fileManager.getPlayerFile(p.getId());
        if (oldFile.getFile().exists()) {
            oldFile.getFile().delete();
            VitalCore.fileManager.getPlayersFiles().remove(oldFile);
        }

        // Crear un nuevo archivo YAML y agregarlo a la lista de archivos
        PlayerFile newFile = new PlayerFile(VitalCore.getPlugin(), p.getId() + ".yml", "users", p);
        VitalCore.fileManager.getPlayersFiles().add(newFile);

        // Reemplazar el documento en la colección
        userCollection.findOneAndReplace(Filters.eq("_id", p.getId()), p);

        return p;
    }

    public static PlayerModel getPlayerWithTribe(UUID playerId) {
        PlayerFile playerFile = VitalCore.fileManager.getPlayerFile(playerId.toString());
        File playerYamlFile = new File(playerFile.getAllPath());

        if (!playerYamlFile.exists()) {
            return null; // El archivo no existe, el jugador no se puede cargar
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            PlayerModel player = mapper.readValue(playerYamlFile, PlayerModel.class);

            if (player != null && player.getTribeId() != null) {
                TribeFile tribeFile = VitalCore.fileManager.getTribeFile(player.getTribeId());
                File tribeYamlFile = new File(tribeFile.getAllPath());

                if (tribeYamlFile.exists()) {
                    TribeModel tribe = mapper.readValue(tribeYamlFile, TribeModel.class);
                    player.setTribe(tribe);
                }
            }

            return player;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<PlayerModel> getAllPlayers() {
        return userCollection.find().into(new ArrayList<PlayerModel>());
    }

}
