package ru.said.xql_crud;

import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import ru.idmt.documino.client.DmConfigureException;
import ru.idmt.documino.client.DocuminoClient;
import ru.idmt.documino.client.api.session.IDmSession;
import ru.idmt.documino.client.api.util.DmException;
import ru.idmt.documino.client.commons.session.DmLoginInfo;
import ru.said.model.Automobiles;
import ru.said.service.AutomobileService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainLayout extends HorizontalLayout {

    private Button add = new Button("Добавить");
    private Button delete = new Button("Удалить");
    private Button edit = new Button("Изменить");
    private TextField search = new TextField();
    private ListDataProvider<Automobiles> dataProvider;
    private List<Automobiles> automobilesList = new ArrayList<>();
    private Grid<Automobiles> grid = new Grid<>();
    private VerticalLayout verticalLayout = new VerticalLayout();
    private HorizontalLayout horizontalLayout = new HorizontalLayout();
    private static final Logger LOGGER = Logger.getLogger(MainLayout.class);
    private Binder<Automobiles> binder = new Binder<>();

    public MainLayout() {
        try {
            IDmSession session = DocuminoClient.get().getDmAdminClient().newSession(new DmLoginInfo(null, null));
            automobilesList = AutomobileService.getAll(session);
            LOGGER.debug("SELECT * FROM ddt_auto");
            dataProvider = new ListDataProvider<>(automobilesList);
            grid.setDataProvider(dataProvider);
            grid.setWidth("525");
            add.addClickListener(clickEvent -> {
                addAuto(session);
            });

            delete.addClickListener(clickEvent -> {
                deleteAuto(session);
            });

            edit.addClickListener(clickEvent -> {
                editAuto(session);
            });

            horizontalLayout.addComponents(add, delete, edit, search);
            verticalLayout.addComponents(horizontalLayout, grid);
            addComponent(verticalLayout);
            grid.addColumn(Automobiles::getId).setCaption("Id");
            grid.addColumn(Automobiles::getModel).setCaption("Model");
            grid.addColumn(Automobiles::getBody).setCaption("Body");

        } catch (DmException | IOException | DmConfigureException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    void initData2() {
        dataProvider = new ListDataProvider<>(automobilesList);
        grid.setDataProvider(dataProvider);
    }

    void addAuto(IDmSession session) {
        final VerticalLayout layout2 = new VerticalLayout();
        final TextField modelTipeTxt = new TextField();
        final TextField bodyTxt = new TextField();
        modelTipeTxt.setCaption("Марка");
        bodyTxt.setCaption("Модель");
        Button save = new Button("Сохранить");
        save.addClickListener(clickEvent -> {
            binder.forField(modelTipeTxt).withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
                    .bind(Automobiles::getModel, Automobiles::setModel);

            binder.forField(bodyTxt)
                    .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
                    .bind(Automobiles::getBody, Automobiles::setBody);
            BinderValidationStatus<Automobiles> status = binder.validate();

            String xql = String.format("CREATE ddt_auto OBJECT SET dss_model = '%s' SET dss_body = '%s'", modelTipeTxt.getValue(), bodyTxt.getValue());
            try {
                if (!status.hasErrors()) {
                    automobilesList = AutomobileService.addRow(session, xql);
                    LOGGER.debug(xql);
                }
            } catch (DmException e) {
                LOGGER.error(e.getMessage());
            } finally {
                if (session != null) {
                    try {
                        session.close();
                    } catch (DmException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
            Page.getCurrent().reload();
            initData2();
        });

        layout2.addComponents(modelTipeTxt, bodyTxt, save);
        addComponent(layout2);
    }

    void deleteAuto(IDmSession session) {
        try {
            String xql = String.format("DELETE ddt_auto OBJECTS WHERE r_object_id = '%s'",
                    grid.getSelectionModel().getFirstSelectedItem().get().getId());

            automobilesList = AutomobileService.deleteRow(session, xql);
            LOGGER.debug(xql);
        } catch (DmException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (DmException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        Page.getCurrent().reload();
        initData2();
    }

    void editAuto(IDmSession session) {
        Button save = new Button("Сохранить");
        final VerticalLayout layout2 = new VerticalLayout();
        final TextField modelTxt = new TextField("Марка");
        final TextField bodyTxt = new TextField("Модель");
        modelTxt.setValue(grid.getSelectedItems().iterator().next().getModel());
        bodyTxt.setValue(grid.getSelectedItems().iterator().next().getBody());
        save.addClickListener(clickEvent -> {
            String model = modelTxt.getValue();
            String body = bodyTxt.getValue();
            String xql = String.format(
                    "UPDATE ddt_auto OBJECTS SET dss_model = '%s' SET dss_body = '%s' WHERE r_object_id = '%s'",
                    model,
                    body,
                    grid.getSelectionModel().getFirstSelectedItem().get().getId());
            try {
                automobilesList = AutomobileService.editRow(session, xql);
                LOGGER.debug(xql);
            } catch (DmException e) {
                LOGGER.error(e.getMessage());
            } finally {
                if (session != null) {
                    try {
                        session.close();
                    } catch (DmException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
            Page.getCurrent().reload();
            initData2();
        });
        layout2.addComponents(modelTxt, bodyTxt, save);
        addComponent(layout2);
    }
}
