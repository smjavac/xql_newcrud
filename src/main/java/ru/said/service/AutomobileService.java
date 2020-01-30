package ru.said.service;

import ru.idmt.documino.client.api.session.IDmSession;
import ru.idmt.documino.client.api.util.DmException;
import ru.idmt.documino.client.commons.operation.GetMapCollection;
import ru.idmt.documino.client.commons.operation.GetString;
import ru.said.model.Automobiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AutomobileService {
    private AutomobileService() {
    }

    public static List<Automobiles> getAll(IDmSession session) throws DmException {
        List<Automobiles> autoList = new ArrayList<>();
        String xql = "SELECT * FROM ddt_auto";
        Collection<Map<String, Object>> autos = new GetMapCollection(xql, new ArrayList<>()).execute(session);
        for (Map<String, Object> autoMap : autos) {
            Automobiles automobile = new Automobiles();
            automobile.setId((String) autoMap.get("r_object_id"));
            automobile.setModel((String) autoMap.get("dss_model"));
            automobile.setBody((String) autoMap.get("dss_body"));
            System.out.println(automobile.getBody());
            autoList.add(automobile);
        }
        return autoList;
    }

    public static List<Automobiles> addRow(IDmSession session, String xql) throws DmException {

        new GetString(xql, null).execute(session);
        return getAll(session);
    }

    public static List<Automobiles> deleteRow(IDmSession session, String xql) throws DmException {
        new GetString(xql, null).execute(session);
        return getAll(session);
    }

    public static List<Automobiles> editRow(IDmSession session, String xql) throws DmException {
        new GetString(xql, null).execute(session);
        return getAll(session);
    }

}
