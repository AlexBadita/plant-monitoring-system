// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require("firebase-functions");

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

exports.temperatureNotification = functions.database.ref('/Devices/{id}').onUpdate(
    (change, context) => {

        const min = Number(change.after.child('/Settings/Temperature/min').val());
        const max = Number(change.after.child('/Settings/Temperature/max').val());
        const temp = Number(change.after.child('/Measurements/temperature').val());
        const enable = Boolean(change.after.child('/Settings/Temperature/enabled').val());
        const name = change.after.child('/name').val();
        const id = context.params.id;

        functions.logger.log(`Min = ${min}, Max = ${max}, Temp = ${temp}, enable = ${enable}, name = ${name}, id = ${id}`);

        if(enable){
            if(temp < min){
                functions.logger.log(`Low Temperature for ${name}!`);
                return admin.messaging().sendToTopic(
                    `temperature_warning${id}`,
                    {
                        notification: {
                            title: `Temperature Warning`,
                            body: `Temperature is too low for ${name}!`
                        }
                    }
                )
            }
            else if(temp > max){
                functions.logger.log(`High Temperature for ${name}!`);
                return admin.messaging().sendToTopic(
                    `temperature_warning${id}`,
                    {
                        notification: {
                            title: `Temperature Warning`,
                            body: `Temperature is too high for ${name}!`
                        }
                    }
                )
            }
        }

        return;
    }
);

exports.humidityNotification = functions.database.ref('/Devices/{id}').onUpdate(
    (change, context) => {

        const min = Number(change.after.child('/Settings/Humidity/min').val());
        const max = Number(change.after.child('/Settings/Humidity/max').val());
        const humidity = Number(change.after.child('/Measurements/humidity').val());
        const enable = Boolean(change.after.child('/Settings/Humidity/enabled').val());
        const name = change.after.child('/name').val();
        const id = context.params.id;

        functions.logger.log(`Min = ${min}, Max = ${max}, Humidity = ${humidity}, enable = ${enable}, name = ${name}, id = ${id}`);

        if(enable){
            if(humidity < min){
                functions.logger.log(`Low Humidity for ${name}!`);
                return admin.messaging().sendToTopic(
                    `humidity_warning${id}`,
                    {
                        notification: {
                            title: `Humidity Warning`,
                            body: `Humidity is too low for ${name}!`
                        }
                    }
                )
            }
            else if(humidity > max){
                functions.logger.log(`High Humidity for ${name}!`);
                return admin.messaging().sendToTopic(
                    `humidity_warning${id}`,
                    {
                        notification: {
                            title: `Humidity Warning`,
                            body: `Humidity is too high for ${name}!`
                        }
                    }
                )
            }
        }

        return;
    }
);

exports.lightNotification = functions.database.ref('/Devices/{id}').onUpdate(
    (change, context) => {

        const min = Number(change.after.child('/Settings/Light/min').val());
        const max = Number(change.after.child('/Settings/Light/max').val());
        const light = Number(change.after.child('/Measurements/light').val());
        const enable = Boolean(change.after.child('/Settings/Light/enabled').val());
        const name = change.after.child('/name').val();
        const id = context.params.id;

        functions.logger.log(`Min = ${min}, Max = ${max}, Light = ${light}, enable = ${enable}, name = ${name}, id = ${id}`);

        if(enable){
            if(light < min){
                functions.logger.log(`Low Light for ${name}!`);
                return admin.messaging().sendToTopic(
                    `Light_warning${id}`,
                    {
                        notification: {
                            title: `Light Warning`,
                            body: `Light is too low for ${name}!`
                        }
                    }
                )
            }
            else if(light > max){
                functions.logger.log(`High Light for ${name}!`);
                return admin.messaging().sendToTopic(
                    `light_warning${id}`,
                    {
                        notification: {
                            title: `Light Warning`,
                            body: `Light is too high for ${name}!`
                        }
                    }
                )
            }
        }

        return;
    }
);