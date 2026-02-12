"use client";

import { Show } from "@refinedev/antd";
import { useShow } from "@refinedev/core";
import { Image, Tag, Typography } from "antd";

const { Title, Text } = Typography;

export default function CatalogShow() {
  const { query } = useShow({});
  const { data, isLoading } = query;
  const record = data?.data;

  return (
    <Show isLoading={isLoading}>
      <Title level={5}>ID</Title>
      <Text>{record?.id}</Text>

      <Title level={5}>Nombre</Title>
      <Text>{record?.title}</Text>

      <Title level={5}>Imagen</Title>
      {record?.image_url ? (
        <Image width={200} src={record.image_url} alt={record.title} />
      ) : (
        <Text>-</Text>
      )}

      <Title level={5}>Duración (días)</Title>
      <Text>{record?.duration_days}</Text>

      <Title level={5}>Ruta</Title>
      <div>
        {record?.route?.map((r: string) => (
          <Tag key={r}>{r}</Tag>
        )) ?? <Text>-</Text>}
      </div>

      <Title level={5}>Moneda</Title>
      <Text>{record?.price_currency ?? "-"}</Text>

      <Title level={5}>Precio base</Title>
      <Text>
        {record?.price_base != null ? `$${record.price_base.toFixed(2)}` : "-"}
      </Text>

      <Title level={5}>Suplemento individual</Title>
      <Text>
        {record?.price_single_supplement != null
          ? `$${record.price_single_supplement.toFixed(2)}`
          : "-"}
      </Text>

      <Title level={5}>Impuestos</Title>
      <Text>
        {record?.price_taxes != null
          ? `$${record.price_taxes.toFixed(2)}`
          : "-"}
      </Text>

      <Title level={5}>Incluye</Title>
      <div>
        {record?.includes?.map((item: string) => (
          <Tag key={item} color="green">
            {item}
          </Tag>
        )) ?? <Text>-</Text>}
      </div>

      <Title level={5}>No incluye</Title>
      <div>
        {record?.not_included?.map((item: string) => (
          <Tag key={item} color="red">
            {item}
          </Tag>
        )) ?? <Text>-</Text>}
      </div>

      <Title level={5}>Requisitos</Title>
      <div>
        {record?.requirements?.map((item: string) => (
          <Tag key={item} color="orange">
            {item}
          </Tag>
        )) ?? <Text>-</Text>}
      </div>

      <Title level={5}>URL de la página</Title>
      <Text>
        {record?.url ? (
          <a href={record.url} target="_blank" rel="noreferrer">
            {record.url}
          </a>
        ) : (
          "-"
        )}
      </Text>

      <Title level={5}>Creado el</Title>
      <Text>{record?.created_at}</Text>

      <Title level={5}>Actualizado el</Title>
      <Text>{record?.updated_at}</Text>
    </Show>
  );
}
