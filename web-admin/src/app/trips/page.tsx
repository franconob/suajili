"use client";

import {
  DeleteButton,
  EditButton,
  List,
  ShowButton,
  useTable,
} from "@refinedev/antd";
import { type BaseRecord } from "@refinedev/core";
import { Avatar, Flex, Space, Table, Tag, Typography } from "antd";
import {
  CalendarOutlined,
  DollarOutlined,
  GlobalOutlined,
  UserOutlined,
} from "@ant-design/icons";

const { Text } = Typography;

const STATUS_MAP: Record<string, { color: string; label: string }> = {
  pending: { color: "orange", label: "Pendiente" },
  confirmed: { color: "blue", label: "Confirmado" },
  paid: { color: "green", label: "Pagado" },
  completed: { color: "default", label: "Completado" },
  cancelled: { color: "red", label: "Cancelado" },
};

const TYPE_MAP: Record<string, { color: string; label: string }> = {
  group: { color: "geekblue", label: "Grupal" },
  custom: { color: "purple", label: "Personalizado" },
};

function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  const d = new Date(value);
  return d.toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatPrice(
  price: number | null | undefined,
  currency: string | null | undefined
): string {
  if (price == null) return "—";
  const sym = currency === "EUR" ? "€" : "$";
  const formatted = price.toLocaleString("es-AR", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  return `${sym} ${formatted} ${currency ?? ""}`.trim();
}

function daysUntil(dateStr: string | null | undefined): number | null {
  if (!dateStr) return null;
  const dep = new Date(dateStr);
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  dep.setHours(0, 0, 0, 0);
  return Math.ceil((dep.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
}

export default function TripsList() {
  const { tableProps } = useTable({
    syncWithLocation: true,
    sorters: { initial: [{ field: "departure_date", order: "asc" }] },
  });

  return (
    <List>
      <Table
        {...tableProps}
        rowKey="id"
        size="middle"
        scroll={{ x: 900 }}
      >
        {/* Viaje: image + name + route */}
        <Table.Column<BaseRecord>
          title="Viaje"
          key="trip"
          width={280}
          render={(_, record) => {
            const name =
              record.type === "group"
                ? record.trip_title ?? "Viaje grupal"
                : record.title ?? "Viaje personalizado";
            const route: string[] | undefined = record.trip_route;
            return (
              <Flex gap={12} align="center">
                {record.trip_image_url ? (
                  <Avatar
                    shape="square"
                    size={48}
                    src={record.trip_image_url}
                  />
                ) : (
                  <Avatar
                    shape="square"
                    size={48}
                    icon={<GlobalOutlined />}
                    style={{ backgroundColor: "#8c8c8c" }}
                  />
                )}
                <div>
                  <Text strong style={{ display: "block" }}>
                    {name}
                  </Text>
                  {route && route.length > 0 && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {route.join(" → ")}
                    </Text>
                  )}
                </div>
              </Flex>
            );
          }}
        />

        {/* Cliente */}
        <Table.Column<BaseRecord>
          title="Cliente"
          dataIndex="user_name"
          key="user_name"
          width={140}
          render={(value: string, record) => (
            <Flex gap={8} align="center">
              <Avatar size="small" icon={<UserOutlined />} />
              <Text>{value || record.user_id?.slice(0, 8)}</Text>
            </Flex>
          )}
        />

        {/* Tipo */}
        <Table.Column<BaseRecord>
          title="Tipo"
          dataIndex="type"
          key="type"
          width={120}
          filters={[
            { text: "Grupal", value: "group" },
            { text: "Personalizado", value: "custom" },
          ]}
          onFilter={(value, record) => record.type === value}
          render={(value: string) => {
            const t = TYPE_MAP[value] ?? { color: "default", label: value };
            return <Tag color={t.color}>{t.label}</Tag>;
          }}
        />

        {/* Estado */}
        <Table.Column<BaseRecord>
          title="Estado"
          dataIndex="status"
          key="status"
          width={120}
          filters={Object.entries(STATUS_MAP).map(([k, v]) => ({
            text: v.label,
            value: k,
          }))}
          onFilter={(value, record) => record.status === value}
          render={(value: string) => {
            const s = STATUS_MAP[value] ?? { color: "default", label: value };
            return <Tag color={s.color}>{s.label}</Tag>;
          }}
        />

        {/* Salida */}
        <Table.Column<BaseRecord>
          title="Salida"
          dataIndex="departure_date"
          key="departure_date"
          width={140}
          sorter
          render={(value: string) => {
            if (!value) return <Text type="secondary">—</Text>;
            const days = daysUntil(value);
            return (
              <div>
                <Flex gap={4} align="center">
                  <CalendarOutlined style={{ fontSize: 12, color: "#8c8c8c" }} />
                  <Text>{formatDate(value)}</Text>
                </Flex>
                {days != null && days >= 0 && days <= 30 && (
                  <Text
                    type={days <= 7 ? "danger" : "warning"}
                    style={{ fontSize: 11 }}
                  >
                    en {days} {days === 1 ? "día" : "días"}
                  </Text>
                )}
              </div>
            );
          }}
        />

        {/* Regreso */}
        <Table.Column
          title="Regreso"
          dataIndex="return_date"
          key="return_date"
          width={130}
          render={(value: string) => (
            <Text type={value ? undefined : "secondary"}>
              {formatDate(value)}
            </Text>
          )}
        />

        {/* Precio */}
        <Table.Column<BaseRecord>
          title="Precio"
          key="price"
          width={130}
          sorter
          render={(_, record) => {
            const text = formatPrice(record.total_price, record.currency);
            return (
              <Flex gap={4} align="center">
                <DollarOutlined
                  style={{ fontSize: 12, color: "#8c8c8c" }}
                />
                <Text>{text}</Text>
              </Flex>
            );
          }}
        />

        {/* Acciones */}
        <Table.Column<BaseRecord>
          title="Acciones"
          key="actions"
          fixed="right"
          width={130}
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
