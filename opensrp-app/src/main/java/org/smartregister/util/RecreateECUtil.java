package org.smartregister.util;

import android.database.Cursor;
import android.support.v4.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.activity.DrishtiApplication;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.repository.EventClientRepository.event_column.baseEntityId;

/**
 * Created by samuelgithengi on 12/30/19.
 */
public class RecreateECUtil {


    public Pair<List<Event>, List<Client>> createEventAndClients(SQLiteDatabase database, String tablename, String eventType, String entityType, FormTag formTag) {

        Table table = DrishtiApplication.getInstance().getClientProcessor().getColumnMappings(tablename);
        if (table == null) {
            return null;
        }
        List<Event> events = new ArrayList<>();
        List<Client> clients = new ArrayList<>();
        List<Map<String, String>> savedEventClients = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(String.format("select * from %s where %s not in (select %s from %s)", tablename, "base_entity_id", baseEntityId, EventClientRepository.Table.event.name()), null);
            int columncount = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                Map<String, String> details = new HashMap<>();
                for (int i = 0; i < columncount; i++) {
                    details.put(cursor.getColumnName(i), cursor.getString(i));
                }
                savedEventClients.add(details);
            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        for (Map<String, String> details : savedEventClients) {
            Event event = new Event().withEventType(eventType).withEntityType(entityType);
            Client client = new Client(details.get("base_entity_id"));
            for (Column column : table.columns) {
                String value = details.get(column.column_name);
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                if (EventClientRepository.Table.event.name().equalsIgnoreCase(column.type)) {
                    populateEvent(column, value, event);
                } else {
                    populateClient(column, value, client);
                }
            }
            tagEvent(event, formTag);
            events.add(event);
            clients.add(client);

        }
        return new Pair<>(events, clients);
    }

    private void tagEvent(Event event, FormTag formTag) {
        event.setProviderId(formTag.providerId);
        event.setLocationId(formTag.locationId);
        event.setChildLocationId(formTag.locationId);
        event.setTeam(formTag.team);
        event.setTeamId(formTag.teamId);
        event.setClientApplicationVersion(formTag.appVersion);
        event.setClientApplicationVersionName(formTag.appVersionName);
        event.setClientDatabaseVersion(formTag.databaseVersion);
    }

    public void saveEventAndClients(Pair<List<Event>, List<Client>> eventClients, SQLiteDatabase sqLiteDatabase) {
        if (eventClients == null) {
            return;
        }
        EventClientRepository eventClientRepository = new EventClientRepository();
        if (eventClients.first != null) {
            Timber.d("saving %d events ", eventClients.first.size());
            eventClientRepository.batchInsertEvents(new JSONArray(eventClients.first), 0, sqLiteDatabase);
        }
        if (eventClients.second != null) {
            Timber.d("saving %d clients", eventClients.second.size());
            eventClientRepository.batchInsertClients(new JSONArray(eventClients.second), sqLiteDatabase);
        }

    }

    private void populateEvent(Column column, String value, Event event) {
        String field = column.json_mapping.field;
        if (field.equalsIgnoreCase("obs.fieldCode")) {
            event.addObs(new Obs().withFieldType(getFieldValue(column.json_mapping.formSubmissionField, column.json_mapping.concept))
                    .withFieldDataType(getFieldValue(column.dataType, "text"))
                    .withFieldCode(column.json_mapping.concept)
                    .withFormSubmissionField(column.json_mapping.formSubmissionField)
                    .withValue(value));

        } else if (field.startsWith("details.")) {
            event.addDetails(field.substring(field.indexOf(".")), value);
        } else if (field.startsWith("identifiers.")) {
            event.addIdentifier(field.substring(field.indexOf(".")), value);
        } else {
            setFieldValue(event, column.json_mapping.field, value);
        }

    }

    private void populateClient(Column column, String value, Client client) {
        String field = column.json_mapping.field;
        if (field.startsWith("attributes.")) {
            client.addAttribute(field.substring(field.indexOf(".")), value);
        } else if (field.startsWith("identifiers.")) {
            client.addIdentifier(field.substring(field.indexOf(".")), value);

        } else if (field.startsWith("relationships.")) {
            client.addRelationship(field.substring(field.indexOf(".")), value);
        } else {
            setFieldValue(client, column.json_mapping.field, value);
        }

    }


    private void setFieldValue(Object instance, String fieldName, String value) {
        if (instance == null || StringUtils.isBlank(fieldName)) {
            return;
        }
        try {
            Field field = getField(instance.getClass(), fieldName);
            if (field != null) {

                field.setAccessible(true);
                field.set(instance, value);
            }
        } catch (IllegalAccessException e) {
            Timber.w(e);
        }
    }

    private Field getField(Class clazz, String fieldName) {
        if (clazz == null || StringUtils.isBlank(fieldName)) {
            return null;
        }

        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // No need to log this, log will be to big
        }
        if (field != null) {
            return field;
        }

        return getField(clazz.getSuperclass(), fieldName);
    }

    private String getFieldValue(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }


}
