package me.winflix.vitalcore.database;

import org.bson.UuidRepresentation;
import org.bukkit.ChatColor;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;

public class Database {

    public static void connect() {
        try {
            MongoClientSettings settings = createMongoClientSettings();

            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("NoWipe");

            UserCollection.initialize(database);
            TribeCollection.initialize(database);

            VitalCore.Log.info(ChatColor.translateAlternateColorCodes('&', "&aConnected to MongoDB!"));
        } catch (MongoException e) {
            VitalCore.Log.info(e.getLocalizedMessage());
        }
    }

    private static MongoClientSettings createMongoClientSettings() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb+srv://Winflix:vjvUdFvRcdHd7gVW@spigotcluster.3drjzzo.mongodb.net/?retryWrites=true&w=majority");
        return MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(connectionString)
                .build();
    }

}
