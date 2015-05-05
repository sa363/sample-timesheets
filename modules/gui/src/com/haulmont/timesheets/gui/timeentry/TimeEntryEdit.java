/*
 * Copyright (c) 2015 com.haulmont.ts.gui.timeentry
 */
package com.haulmont.timesheets.gui.timeentry;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.LookupPickerField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.timesheets.entity.*;
import com.haulmont.timesheets.gui.ComponentsHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorelov
 */
public class TimeEntryEdit extends AbstractEditor<TimeEntry> {

    @Inject
    protected FieldGroup fieldGroup;
    @Inject
    protected UserSession userSession;
    @Inject
    protected Datasource<TimeEntry> timeEntryDs;
    @Inject
    protected CollectionDatasource<Tag, UUID> tagsDs;
    @Inject
    protected CollectionDatasource<Tag, UUID> allTagsDs;

    @Named("fieldGroup.task")
    protected LookupPickerField taskField;

    @Override
    public void init(Map<String, Object> params) {
        taskField.addAction(ComponentsHelper.createLookupAction(taskField));
        taskField.addClearAction();
        fieldGroup.addCustomField("description", ComponentsHelper.getCustomTextArea());

        timeEntryDs.addListener(new DsListenerAdapter<TimeEntry>() {
            @Override
            public void valueChanged(TimeEntry source, String property, Object prevValue, Object value) {
                if ("task".equals(property)) {
                    // #PL-5355
                    tagsDs.clear();
                    if (value != null) {
                        Task task = (Task) value;
                        for (Tag tag : task.getDefaultTags()) {
                            tagsDs.includeItem(tag);
                        }

                        List<UUID> ids = null;
                        if (!task.getRequiredTagTypes().isEmpty()) {
                            ids = new ArrayList<>();
                            for (TagType type : task.getRequiredTagTypes()) {
                                ids.add(type.getId());
                            }
                        }
                        allTagsDs.refresh(ParamsMap.of("requiredTagTypes", ids));
                    }
                }
            }
        });
    }

    @Override
    protected void postInit() {
        super.postInit();
        TimeEntry timeEntry = getItem();
        if (timeEntry.getStatus() == null) {
            timeEntry.setStatus(TimeEntryStatus.NEW);
        }
        if (timeEntry.getUser() == null) {
            timeEntry.setUser(userSession.getUser());
        }
    }
}