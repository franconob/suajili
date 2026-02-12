"use client";

import {
  DeleteButton,
  EditButton,
  List,
  ShowButton,
  useTable,
} from "@refinedev/antd";
import { type BaseRecord } from "@refinedev/core";
import { Image, Space, Table, Tag } from "antd";

export default function CatalogList() {
  const { tableProps } = useTable({ syncWithLocation: true });

  return (
    <List>
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="id" title="ID" />
        <Table.Column dataIndex="title" title="Nombre" />
        <Table.Column
          dataIndex="image_url"
          title="Imagen"
          render={(value: string) =>
            value ? (
              <Image width={80} src={value} alt="trip" />
            ) : (
              "-"
            )
          }
        />
        <Table.Column dataIndex="duration_days" title="Días" />
        <Table.Column
          dataIndex="route"
          title="Ruta"
          render={(value: string[]) =>
            value?.map((r) => <Tag key={r}>{r}</Tag>) ?? "-"
          }
        />
        <Table.Column dataIndex="price_currency" title="Moneda" />
        <Table.Column
          dataIndex="price_base"
          title="Precio base"
          render={(value: number) =>
            value != null ? `$${value.toFixed(2)}` : "-"
          }
        />
        <Table.Column<BaseRecord>
          title="Acciones"
          render={(_, record) => (
            <Space>
              <ShowButton hideText size="small" recordItemId={record.id} />
              <EditButton hideText size="small" recordItemId={record.id} />
              <DeleteButton hideText size="small" recordItemId={record.id} />
            </Space>
          )}
        />
      </Table>
    </List>
  );
}
