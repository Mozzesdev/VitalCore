package me.winflix.vitalcore.database.collections;

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

import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import me.winflix.vitalcore.utils.RankManager;

public class TribeCollection {

    public static MongoCollection<TribeModel> tribesCollection;

    public static void initialize(MongoDatabase database) {
        PojoCodecProvider pojoAutoProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoAutoProvider));

        tribesCollection = database.getCollection("Tribes", TribeModel.class).withCodecRegistry(pojoCodecRegistry);
    }

    public static TribeModel createTribe(Player player) {
        TribeMember owner = new TribeMember(player.getDisplayName(), player.getUniqueId().toString());
        String tribeName = "Tribe_of_" + owner.getPlayerName();
        owner.setRange(RankManager.OWNER_RANK);
        TribeModel tribeModel = new TribeModel(tribeName, UUID.randomUUID().toString());
        tribeModel.addMember(owner);
        tribesCollection.insertOne(tribeModel);
        return tribeModel;
    }

    public static TribeModel getTribeById(String id) {
        return tribesCollection.find(Filters.eq("_id", id)).first();
    }

    public static TribeModel getTribeByName(String name) {
        return tribesCollection.find(Filters.eq("tribeName", name)).first();
    }

    public static TribeModel saveTribe(TribeModel tribe) {
        return tribesCollection.findOneAndReplace(Filters.eq("_id", tribe.getId()), tribe);
    }

    public static void deleteTribe(TribeModel t) {
        tribesCollection.deleteOne(Filters.eq("_id", t.getId()));
    }

    public static ArrayList<TribeModel> getAllTribes() {
        return tribesCollection.find().into(new ArrayList<TribeModel>());
    }

}
