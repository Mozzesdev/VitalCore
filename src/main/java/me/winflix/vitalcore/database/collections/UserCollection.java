package me.winflix.vitalcore.database.collections;

import java.util.UUID;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.entity.Player;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

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
        PlayerModel playerModel = new PlayerModel(player.getDisplayName(), uuid.toString(), tribe.getId());
        userCollection.insertOne(playerModel);
        return playerModel;
    }

    public static PlayerModel savePlayer(PlayerModel p) {
        return userCollection.findOneAndReplace(Filters.eq("_id", p.getId()), p);
    }

    
    public static PlayerModel getPlayerWithTribe(UUID playerId) {
        PlayerModel player = getPlayer(playerId);
        
        if (player != null) {
            TribeModel tribe = TribeCollection.getTribeById(player.getTribeId());
            player.setTribe(tribe);
        }
        
        return player;
    }

}
