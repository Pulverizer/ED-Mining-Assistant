package io.github.pulverizer.ring_finder;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.pulverizer.ring_finder.objects.Belt;
import io.github.pulverizer.ring_finder.objects.Body;
import io.github.pulverizer.ring_finder.objects.System;
import io.github.pulverizer.ring_finder.utils.BeltType;
import io.github.pulverizer.ring_finder.utils.ReserveLevel;
import io.github.pulverizer.ring_finder.utils.SecurityLevel;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.GZIPInputStream;

class DataFilter {
    //Mass: Mt
    //Arrival Distance: Ls / Lightseconds
    //Radius: Km

    private static final String POPULATED_DOWNLOAD_URL = "https://www.edsm.net/dump/systemsPopulated.json.gz";
    private static final String OTHER_DOWNLOAD_URL = "https://www.edsm.net/dump/systemsWithCoordinates7days.json.gz";

    public static final File POPULATED_FILE = new File(POPULATED_DOWNLOAD_URL.substring(POPULATED_DOWNLOAD_URL.lastIndexOf('/') + 1, POPULATED_DOWNLOAD_URL.length() - 3));
    public static final File OTHER_FILE = new File(OTHER_DOWNLOAD_URL.substring(OTHER_DOWNLOAD_URL.lastIndexOf('/') + 1, OTHER_DOWNLOAD_URL.length() - 3));


    private static final ArrayList<SecurityLevel> SECURITY_LEVELS = new ArrayList<>(Arrays.asList(SecurityLevel.NULL, SecurityLevel.Anarchy, SecurityLevel.Lawless, SecurityLevel.Low, SecurityLevel.Medium, SecurityLevel.High));
    private static final ArrayList<ReserveLevel> RESERVE_LEVELS = new ArrayList<>(Arrays.asList(ReserveLevel.NULL, ReserveLevel.Depleted, ReserveLevel.Low, ReserveLevel.Common, ReserveLevel.Major, ReserveLevel.Pristine));


    private boolean searchOnlyPopulatedSystems = true;
    private SecurityLevel minimumSecurityLevel = SecurityLevel.NULL;
    private BeltType beltType = BeltType.Icy;
    private ReserveLevel minimumReserveLevel = ReserveLevel.NULL;
    private float minimumSizeDensityIndex = 0;
    private float minimumDensity = 0;
    private boolean refreshData = false;


    private File dataFile;
    private Path zipFile;
    private Set<System> data;


    private int systemCounter;

    public void downloadNightlyDump() throws IOException {
        String urlString = searchOnlyPopulatedSystems ? POPULATED_DOWNLOAD_URL : OTHER_DOWNLOAD_URL;
        URL url = new URL(urlString);

        String zipFileName = urlString.substring(urlString.lastIndexOf('/') + 1);
        zipFile = new File(zipFileName).toPath();
        Files.copy(url.openStream(), zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public void unzipFile() throws IOException {
        byte[] buffer = new byte[1024];

        FileInputStream fileIn = new FileInputStream(zipFile.toString());
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);

        dataFile = searchOnlyPopulatedSystems ? POPULATED_FILE : OTHER_FILE;
        FileOutputStream fileOutputStream = new FileOutputStream(dataFile);

        int bytes_read;
        while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {

            fileOutputStream.write(buffer, 0, bytes_read);
        }

        gZIPInputStream.close();
        fileOutputStream.close();
    }

    public void loadJSON() throws IOException {
        systemCounter = 0;

        dataFile = searchOnlyPopulatedSystems ? POPULATED_FILE : OTHER_FILE;
        data = new HashSet<>();
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);

        JsonReader jsonReader = new JsonReader(streamReader);

        jsonReader.beginArray();
        Gson gson = new GsonBuilder().create();

        while (jsonReader.hasNext()) {
            JsonObject systemNode = gson.fromJson(jsonReader, JsonObject.class);

            if (systemNode.has("id") && systemNode.has("name") && systemNode.has("security")) {
                systemCounter++;

                System system = new System(
                        systemNode.get("id").getAsInt(),
                        systemNode.get("name").getAsString(),
                        systemNode.get("security").getAsString());

                boolean meetsSecurityRequirement = SECURITY_LEVELS.indexOf(system.getSecurity()) >= SECURITY_LEVELS.indexOf(minimumSecurityLevel);

                if (meetsSecurityRequirement) {

                    if (systemNode.has("allegiance") && !systemNode.get("allegiance").isJsonNull()) {
                        String allegiance = systemNode.get("allegiance").getAsString();
                        system.setAllegiance(allegiance);
                    }

                    if (systemNode.has("bodies") && !systemNode.get("bodies").isJsonNull()) {
                        systemNode.get("bodies").getAsJsonArray().forEach(bodyArrayNode -> {
                            JsonObject bodyNode = bodyArrayNode.getAsJsonObject();

                            Body body = new Body(
                                    bodyNode.get("id").getAsInt(),
                                    bodyNode.get("name").getAsString(),
                                    bodyNode.get("distanceToArrival").getAsInt());

                            if (bodyNode.has("reserveLevel") && !bodyNode.get("reserveLevel").isJsonNull()) {
                                body.setReserveLevel(bodyNode.get("reserveLevel").getAsString());

                                if (RESERVE_LEVELS.indexOf(body.getReserveLevel()) >= RESERVE_LEVELS.indexOf(minimumReserveLevel)) {

                    /*if (bodyNode.has("belts") && !bodyNode.get("belts").isJsonNull()) {
                        bodyNode.get("belts").getAsJsonArray().forEach(beltArrayNode -> {
                            JsonObject beltNode = beltArrayNode.getAsJsonObject();

                            Belt belt = new Belt(
                                    beltNode.get("name").getAsString(),
                                    beltNode.get("type").getAsString(),
                                    beltNode.get("mass").getAsFloat(),
                                    beltNode.get("innerRadius").getAsInt(),
                                    beltNode.get("outerRadius").getAsInt());

                            body.belts().add(belt);
                        });
                    }*/

                                    if (bodyNode.has("rings") && !bodyNode.get("rings").isJsonNull()) {
                                        bodyNode.get("rings").getAsJsonArray().forEach(beltArrayNode -> {
                                            JsonObject beltNode = beltArrayNode.getAsJsonObject();

                                            Belt belt = new Belt(
                                                    beltNode.get("name").getAsString(),
                                                    beltNode.get("type").getAsString(),
                                                    beltNode.get("mass").getAsFloat(),
                                                    beltNode.get("innerRadius").getAsInt(),
                                                    beltNode.get("outerRadius").getAsInt());

                                            if (belt.getType() == beltType && belt.getDensity() > minimumDensity && belt.getSizeDensityIndex() > minimumSizeDensityIndex) {
                                                body.belts().add(belt);
                                            }
                                        });
                                    }

                                    if (body.hasAsteroids()) {
                                        system.bodies().add(body);
                                    }
                                }
                            }
                        });
                    }
                }

                if (meetsSecurityRequirement && !system.bodies().isEmpty()) {
                    data.add(system);
                }
            }
        }
    }

    public void setShouldRefreshData(boolean shouldRefreshData) {
        refreshData = shouldRefreshData;
    }

    public boolean shouldRefreshData() {
        if (refreshData) {
            return true;

        } else if (searchOnlyPopulatedSystems) {
            return !POPULATED_FILE.exists() || java.lang.System.currentTimeMillis() - POPULATED_FILE.lastModified() > 86400000;

        } else {
            return !OTHER_FILE.exists() || java.lang.System.currentTimeMillis() - OTHER_FILE.lastModified() > 86400000;
        }
    }

    public void setMinimumDensity(float minimumDensity) {
        this.minimumDensity = minimumDensity;
    }

    public void setMinimumSizeDensityIndex(float minimumSizeDensityIndex) {
        this.minimumSizeDensityIndex = minimumSizeDensityIndex;
    }

    public void setShouldSearchOnlyPopulatedSystems(boolean searchOnlyPopulatedSystems) {
        this.searchOnlyPopulatedSystems = searchOnlyPopulatedSystems;
    }

    public boolean shouldSearchOnlyPopulatedSystems() {
        return searchOnlyPopulatedSystems;
    }

    public void setMinimumSecurityLevel(SecurityLevel minimumSecurityLevel) {
        this.minimumSecurityLevel = minimumSecurityLevel;
    }

    public void setBeltType(BeltType beltType) {
        this.beltType = beltType;
    }

    public void setMinimumReserveLevel(ReserveLevel minimumReserveLevel) {
        this.minimumReserveLevel = minimumReserveLevel;
    }

    public Set<System> getData() {
        return data;
    }

    public SecurityLevel getMinimumSecurityLevel() {
        return minimumSecurityLevel;
    }

    public BeltType getBeltType() {
        return beltType;
    }

    public ReserveLevel getMinimumReserveLevel() {
        return minimumReserveLevel;
    }

    public float getMinimumSizeDensityIndex() {
        return minimumSizeDensityIndex;
    }

    public float getMinimumDensity() {
        return minimumDensity;
    }

    public int getSystemCount() {
        return systemCounter;
    }
}
