/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.uniquerows;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineHopMeta;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageDialogWithToggle;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

public class UniqueRowsDialog extends BaseTransformDialog implements ITransformDialog {
  private static final Class<?> PKG = UniqueRowsMeta.class; // For Translator

  public static final String STRING_SORT_WARNING_PARAMETER = "UniqueSortWarning";

  private final UniqueRowsMeta input;

  private Button wCount;

  private Label wlCountField;
  private Text wCountField;

  private TableView wFields;

  private ColumnInfo[] colinf;

  private final Map<String, Integer> inputFields;

  private Button wRejectDuplicateRow;

  private Label wlErrorDesc;
  private TextVar wErrorDesc;

  public UniqueRowsDialog(
      Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
    super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
    input = (UniqueRowsMeta) in;
    inputFields = new HashMap<>();
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = e -> input.setChanged();
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = props.getMargin();

    // TransformName line
    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.TransformName.Label"));
    props.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);
    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    props.setLook(wTransformName);
    wTransformName.addModifyListener(lsMod);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    fdTransformName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdTransformName);

    // ///////////////////////////////
    // START OF Settings GROUP //
    // ///////////////////////////////

    Group wSettings = new Group(shell, SWT.SHADOW_NONE);
    props.setLook(wSettings);
    wSettings.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.Settings.Label"));

    FormLayout settingsgroupLayout = new FormLayout();
    settingsgroupLayout.marginWidth = 10;
    settingsgroupLayout.marginHeight = 10;
    wSettings.setLayout(settingsgroupLayout);

    Label wlCount = new Label(wSettings, SWT.RIGHT);
    wlCount.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.Count.Label"));
    props.setLook(wlCount);
    FormData fdlCount = new FormData();
    fdlCount.left = new FormAttachment(0, 0);
    fdlCount.top = new FormAttachment(wTransformName, margin);
    fdlCount.right = new FormAttachment(middle, -margin);
    wlCount.setLayoutData(fdlCount);
    wCount = new Button(wSettings, SWT.CHECK);
    props.setLook(wCount);
    wCount.setToolTipText(BaseMessages.getString(PKG, "UniqueRowsDialog.Count.ToolTip", Const.CR));
    FormData fdCount = new FormData();
    fdCount.left = new FormAttachment(middle, 0);
    fdCount.top = new FormAttachment(wlCount, 0, SWT.CENTER);
    wCount.setLayoutData(fdCount);
    wCount.addListener(
        SWT.Selection,
        e -> {
          input.setChanged();
          setFlags();
        });

    wlCountField = new Label(wSettings, SWT.LEFT);
    wlCountField.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.CounterField.Label"));
    props.setLook(wlCountField);
    FormData fdlCountField = new FormData();
    fdlCountField.left = new FormAttachment(wCount, margin);
    fdlCountField.top = new FormAttachment(wTransformName, margin);
    wlCountField.setLayoutData(fdlCountField);
    wCountField = new Text(wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wCountField);
    wCountField.addModifyListener(lsMod);
    FormData fdCountField = new FormData();
    fdCountField.left = new FormAttachment(wlCountField, margin);
    fdCountField.top = new FormAttachment(wTransformName, margin);
    fdCountField.right = new FormAttachment(100, 0);
    wCountField.setLayoutData(fdCountField);

    Label wlRejectDuplicateRow = new Label(wSettings, SWT.RIGHT);
    wlRejectDuplicateRow.setText(
        BaseMessages.getString(PKG, "UniqueRowsDialog.RejectDuplicateRow.Label"));
    props.setLook(wlRejectDuplicateRow);
    FormData fdlRejectDuplicateRow = new FormData();
    fdlRejectDuplicateRow.left = new FormAttachment(0, 0);
    fdlRejectDuplicateRow.top = new FormAttachment(wCountField, margin);
    fdlRejectDuplicateRow.right = new FormAttachment(middle, -margin);
    wlRejectDuplicateRow.setLayoutData(fdlRejectDuplicateRow);
    wRejectDuplicateRow = new Button(wSettings, SWT.CHECK);
    props.setLook(wRejectDuplicateRow);
    wRejectDuplicateRow.setToolTipText(
        BaseMessages.getString(PKG, "UniqueRowsDialog.RejectDuplicateRow.ToolTip", Const.CR));
    FormData fdRejectDuplicateRow = new FormData();
    fdRejectDuplicateRow.left = new FormAttachment(middle, 0);
    fdRejectDuplicateRow.top = new FormAttachment(wlRejectDuplicateRow, 0, SWT.CENTER);
    wRejectDuplicateRow.setLayoutData(fdRejectDuplicateRow);
    wRejectDuplicateRow.addListener(
        SWT.Selection,
        e -> {
          input.setChanged();
          setErrorDesc();
        });

    wlErrorDesc = new Label(wSettings, SWT.LEFT);
    wlErrorDesc.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.ErrorDescription.Label"));
    props.setLook(wlErrorDesc);
    FormData fdlErrorDesc = new FormData();
    fdlErrorDesc.left = new FormAttachment(wRejectDuplicateRow, margin);
    fdlErrorDesc.top = new FormAttachment(wCountField, margin);
    wlErrorDesc.setLayoutData(fdlErrorDesc);
    wErrorDesc = new TextVar(variables, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wErrorDesc);
    wErrorDesc.addModifyListener(lsMod);
    FormData fdErrorDesc = new FormData();
    fdErrorDesc.left = new FormAttachment(wlErrorDesc, margin);
    fdErrorDesc.top = new FormAttachment(wCountField, margin);
    fdErrorDesc.right = new FormAttachment(100, 0);
    wErrorDesc.setLayoutData(fdErrorDesc);

    FormData fdSettings = new FormData();
    fdSettings.left = new FormAttachment(0, margin);
    fdSettings.top = new FormAttachment(wTransformName, margin);
    fdSettings.right = new FormAttachment(100, -margin);
    wSettings.setLayoutData(fdSettings);

    // ///////////////////////////////////////////////////////////
    // / END OF Settings GROUP
    // ///////////////////////////////////////////////////////////

    // Some buttons
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wGet = new Button(shell, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.Get.Button"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    setButtonPositions(new Button[] {wOk, wGet, wCancel}, margin, null);

    Label wlFields = new Label(shell, SWT.NONE);
    wlFields.setText(BaseMessages.getString(PKG, "UniqueRowsDialog.Fields.Label"));
    props.setLook(wlFields);
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment(0, 0);
    fdlFields.top = new FormAttachment(wSettings, margin);
    wlFields.setLayoutData(fdlFields);

    final int fieldsRows = input.getCompareFields() == null ? 0 : input.getCompareFields().size();

    colinf =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "UniqueRowsDialog.ColumnInfo.Fieldname"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              new String[] {""},
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "UniqueRowsDialog.ColumnInfo.IgnoreCase"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              new String[] {"Y", "N"},
              true)
        };

    wFields =
        new TableView(
            variables,
            shell,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
            colinf,
            fieldsRows,
            lsMod,
            props);

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(100, 0);
    fdFields.bottom = new FormAttachment(wOk, -2 * margin);
    wFields.setLayoutData(fdFields);

    //
    // Search the fields in the background

    final Runnable runnable =
        () -> {
          TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
          if (transformMeta != null) {
            try {
              IRowMeta row = pipelineMeta.getPrevTransformFields(variables, transformMeta);

              // Remember these fields...
              for (int i = 0; i < row.size(); i++) {
                inputFields.put(row.getValueMeta(i).getName(), i);
              }
              setComboBoxes();
            } catch (HopException e) {
              logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
            }
          }
        };
    new Thread(runnable).start();

    // Add listeners
    wCancel.addListener(SWT.Selection, e -> cancel());
    wGet.addListener(SWT.Selection, e -> get());
    wOk.addListener(SWT.Selection, e -> ok());

    getData();
    input.setChanged(changed);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private void setErrorDesc() {
    wlErrorDesc.setEnabled(wRejectDuplicateRow.getSelection());
    wErrorDesc.setEnabled(wRejectDuplicateRow.getSelection());
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<>(keySet);

    String[] fieldNames = entries.toArray(new String[entries.size()]);

    Const.sortStrings(fieldNames);
    colinf[0].setComboValues(fieldNames);
  }

  public void setFlags() {
    wlCountField.setEnabled(wCount.getSelection());
    wCountField.setEnabled(wCount.getSelection());
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    wCount.setSelection(input.isCountRows());
    if (input.getCountField() != null) {
      wCountField.setText(input.getCountField());
    }
    setFlags();
    wRejectDuplicateRow.setSelection(input.isRejectDuplicateRow());
    if (input.getErrorDescription() != null) {
      wErrorDesc.setText(input.getErrorDescription());
    }
    setErrorDesc();
    int i = 0;
    for (UniqueField field : input.getCompareFields()) {
      TableItem item = wFields.table.getItem(i++);
      if (field != null) {
        item.setText(1, field.getName());
      }
      item.setText(2, field.isCaseInsensitive() ? "Y" : "N");
    }
    wFields.setRowNums();
    wFields.optWidth(true);

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    int nrFields = wFields.nrNonEmpty();
    List<UniqueField> fields = new ArrayList<>(nrFields);
    for (int i = 0; i < nrFields; i++) {
      TableItem item = wFields.getNonEmpty(i);
      UniqueField field = new UniqueField();
      field.setName(item.getText(1));
      field.setCaseInsensitive("Y".equalsIgnoreCase(item.getText(2)));
      fields.add(field);
    }

    input.setCountField(wCountField.getText());
    input.setCountRows(wCount.getSelection());
    input.setRejectDuplicateRow(wRejectDuplicateRow.getSelection());
    input.setErrorDescription(wErrorDesc.getText());
    input.setCompareFields(fields);
    transformName = wTransformName.getText(); // return value

    if ("Y".equalsIgnoreCase(props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y"))) {
      MessageDialogWithToggle md =
          new MessageDialogWithToggle(
              shell,
              BaseMessages.getString(PKG, "UniqueRowsDialog.InputNeedSort.DialogTitle"),
              BaseMessages.getString(PKG, "UniqueRowsDialog.InputNeedSort.DialogMessage", Const.CR)
                  + Const.CR,
              SWT.ICON_WARNING,
              new String[] {BaseMessages.getString(PKG, "UniqueRowsDialog.InputNeedSort.Option1")},
              BaseMessages.getString(PKG, "UniqueRowsDialog.InputNeedSort.Option2"),
              "N".equalsIgnoreCase(props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y")));
      md.open();
      props.setCustomParameter(STRING_SORT_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y");
    }

    // Remove any error hops coming out of UniqueRows when Reject Duplicate Rows checkbox is
    // unselected.
    if (!wRejectDuplicateRow.getSelection()) {
      List<PipelineHopMeta> hops = this.pipelineMeta.getPipelineHops();
      IntStream.range(0, hops.size())
          .filter(
              hopInd -> {
                PipelineHopMeta hop = hops.get(hopInd);
                return (hop.isErrorHop()
                    && hop.getFromTransform()
                        .getTransformPluginId()
                        .equals(this.input.getParentTransformMeta().getTransformPluginId()));
              })
          .forEach(hopInd -> this.pipelineMeta.removePipelineHop(hopInd));
    }

    dispose();
  }

  private void get() {
    try {
      IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
      if (r != null && !r.isEmpty()) {
        BaseTransformDialog.getFieldsFromPrevious(
            r, wFields, 1, new int[] {1}, new int[] {}, -1, -1, null);
      }
    } catch (HopException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "UniqueRowsDialog.FailedToGetFields.DialogTitle"),
          BaseMessages.getString(PKG, "UniqueRowsDialog.FailedToGetFields.DialogMessage"),
          ke);
    }
  }
}
