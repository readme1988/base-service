package script.db

databaseChangeLog(logicalFilePath: 'script/db/fd_lookup_value.groovy') {
    changeSet(author: 'superleader8@gmail.com', id: '2018-03-19-fd-lookup-value') {
        if(helper.dbType().isSupportSequence()){
            createSequence(sequenceName: 'FD_LOOKUP_VALUE_S', startValue:"1")
        }
        createTable(tableName: 'FD_LOOKUP_VALUE') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', autoIncrement: true, remarks: '表ID，主键，供其他表做外键，unsigned bigint、单表时自增、步长为 1') {
                constraints(primaryKey: true, primaryKeyName: 'PK_FD_LOOKUP_VALUE')
            }
            column(name: 'LOOKUP_ID', type: 'BIGINT UNSIGNED', remarks: '值名称') {
                constraints(nullable: false)
            }
            column(name: 'CODE', type: 'VARCHAR(32)', remarks: '代码') {
                constraints(nullable: false)
            }
            column(name: 'DESCRIPTION', type: 'VARCHAR(256)', remarks: '描述')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createTable(tableName: 'FD_LOOKUP_VALUE_TL') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '关联lookup_value id') {
                constraints(nullable: false)
            }
            column(name: 'LANG', type: 'VARCHAR(16)', remarks: '语言名称') {
                constraints(nullable: false)
            }
            column(name: 'DESCRIPTION', type: 'VARCHAR(255)', remarks: '描述')
        }
        addUniqueConstraint(tableName: 'FD_LOOKUP_VALUE', columnNames: 'CODE,LOOKUP_ID', constraintName: 'UK_FD_LOOKUP_VALUE_U1')
        addPrimaryKey(tableName: 'FD_LOOKUP_VALUE_TL', columnNames: 'ID, LANG', constraintName: 'PK_FD_LOOKUP_VALUE_TL')
    }

    changeSet(author: 'superlee', id: '2019-04-24-fd-lookup-value-tl-add-column') {
        addColumn(tableName: 'FD_LOOKUP_VALUE_TL') {
            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT UNSIGNED", defaultValue: "1") {
                constraints(nullable: true)
            }
            column(name: "CREATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0") {
                constraints(nullable: true)
            }
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0") {
                constraints(nullable: true)
            }
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'superlee', id: '2019-07-18-fd-lookup-value-add-remark') {
        setTableRemarks(tableName:"FD_LOOKUP_VALUE",remarks: "快码值表")
        setTableRemarks(tableName:"FD_LOOKUP_VALUE_TL",remarks: "快码值表的多语言表")
    }

    changeSet(author:  "hailor",id: "2019-09-06-fd-lookup-value-tl-add-column"){
        addColumn(tableName: 'FD_LOOKUP_VALUE') {
            column(name: 'DISPLAY_ORDER', type: "decimal(20,0)", remarks: '显示顺序')
        }
    }

    changeSet(author: "bgzyy", id: "2019-10-30-fd-lookup-value-upd-column") {
        renameColumn(tableName: 'FD_LOOKUP_VALUE', oldColumnName: 'DISPLAY_ORDER', newColumnName: 'DISPLAY_ORDER', columnDataType: 'INT UNSIGNED')
        addDefaultValue(tableName: 'FD_LOOKUP_VALUE', columnName: 'DISPLAY_ORDER', defaultValue: '1')
    }
}