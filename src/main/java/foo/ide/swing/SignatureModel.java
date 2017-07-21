package foo.ide.swing;

import foo.model.FunctionNode;

import javax.swing.table.DefaultTableModel;

class SignatureModel extends DefaultTableModel {
    SignatureModel(FunctionNode fn) {
        this.addColumn("Name");
        this.addColumn("Description");

        this.addRow(new Object[] {fn.name(), fn.getComment()});

        fn.parameters().forEach(param ->
            this.addRow(new Object[] {param.name(), param.getComment()})
        );
    }
}
