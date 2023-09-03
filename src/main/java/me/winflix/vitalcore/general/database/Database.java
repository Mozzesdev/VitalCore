package me.winflix.vitalcore.general.database;

import org.bson.UuidRepresentation;
import org.bukkit.ChatColor;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;

public class Database {

    public static void connect() {
        try {
            MongoClientSettings settings = createMongoClientSettings();

            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("VitalCore");

            UsersCollection.initialize(database);
            TribesCollection.initialize(database);

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
