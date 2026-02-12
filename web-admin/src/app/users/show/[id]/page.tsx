"use client";

import { Show } from "@refinedev/antd";
import { useShow } from "@refinedev/core";
import { Typography } from "antd";

const { Title, Text } = Typography;

export default function UserShow() {
  const { query } = useShow({});
  const { data, isLoading } = query;
  const record = data?.data;

  return (
    <Show isLoading={isLoading}>
      <Title level={5}>ID</Title>
      <Text>{record?.id}</Text>

      <Title level={5}>Email</Title>
      <Text>{record?.email}</Text>

      <Title level={5}>Phone</Title>
      <Text>{record?.phone ?? "-"}</Text>

      <Title level={5}>First Name</Title>
      <Text>{record?.first_name ?? "-"}</Text>

      <Title level={5}>Last Name</Title>
      <Text>{record?.last_name ?? "-"}</Text>
    </Show>
  );
}
