const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();
const messaging = getMessaging();

exports.sendFcmOnLocationCreate = onDocumentCreated("locations/{locationId}", async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
        return;
    }

    const newLocation = snapshot.data();
    const locationId = snapshot.id;

    const postCreatorId = newLocation.uid;

    if (
        !newLocation ||
        typeof newLocation.latitude !== "number" ||
        typeof newLocation.longitude !== "number"
    ) {
        return null;
    }

    const usersSnapshot = await db.collection("users").get();
    const tokensToSend = [];

    usersSnapshot.docs.forEach((doc) => {
        const user = doc.data();
        const token = user.fcmToken;
        const lat = user.latitude;
        const lon = user.longitude;
        const userUid = doc.id;

        if (userUid === postCreatorId) {
            return;
        }

        if (!token || typeof lat !== "number" || typeof lon !== "number") {
            return;
        }

        const distance = calculateDistance(
            lat,
            lon,
            newLocation.latitude,
            newLocation.longitude
        );

        if (distance <= 10000) {
            tokensToSend.push(token);
        }
    });

    if (tokensToSend.length > 0) {
        const message = {
            notification: {
                title: newLocation.type || "Nova lokacija",
                body: "Pojavila se nova gužva u vašoj blizini!",
            },
            data: {
                locationId: locationId,
            },
            tokens: tokensToSend,
        };

        try {
            const response = await messaging.sendEachForMulticast(message);
        } catch (error) {
            console.error("Greška pri slanju notifikacija:", error);
        }
    }
    return null;
});

exports.checkNearbyLocationsOnUserUpdate = onDocumentUpdated("users/{userId}", async (event) => {
    const userId = event.params.userId;
    const userDataBefore = event.data.before.data();
    const userDataAfter = event.data.after.data();

    const userLatBefore = userDataBefore.latitude;
    const userLonBefore = userDataBefore.longitude;
    const userLatAfter = userDataAfter.latitude;
    const userLonAfter = userDataAfter.longitude;
    const userToken = userDataAfter.fcmToken;

    if (!userLatAfter || !userLonAfter || !userToken || !userLatBefore || !userLonBefore) {
        return null;
    }

    const locationsSnapshot = await db.collection("locations").get();
    const tokensToSend = [];
    const nearbyLocationTypes = [];

    locationsSnapshot.docs.forEach((doc) => {
        const locationData = doc.data();
        const locationLat = locationData.latitude;
        const locationLon = locationData.longitude;
        const locationType = locationData.type || "Gužva";

        if (!locationLat || !locationLon) {
            return;
        }

        const distanceBefore = calculateDistance(
            userLatBefore,
            userLonBefore,
            locationLat,
            locationLon
        );

        const distanceAfter = calculateDistance(
            userLatAfter,
            userLonAfter,
            locationLat,
            locationLon
        );

        if (distanceBefore > 10000 && distanceAfter <= 10000) {
            if (!tokensToSend.includes(userToken)) {
                tokensToSend.push(userToken);
            }
            nearbyLocationType = locationData.type || "Gužva"
        }
    });

    if (tokensToSend.length > 0) {
        const message = {
            notification: {
                title: `Gužva u vašoj blizini!`,
                body: `Približili ste se gužvi: ${nearbyLocationType}.`,
            },
            tokens: tokensToSend,
        };

        try {
            await messaging.sendEachForMulticast(message);
        } catch (error) {
            console.error("Greška pri slanju notifikacija:", error);
        }
    }

    return null;
});

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) *
        Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}