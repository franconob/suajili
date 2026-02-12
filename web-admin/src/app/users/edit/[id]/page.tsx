"use client";

import { Edit, useForm } from "@refinedev/antd";
import { Form, Input } from "antd";

export default function UserEdit() {
  const { formProps, saveButtonProps, query } = useForm({});
  const record = query?.data?.data;

  return (
    <Edit saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="Email">
          <Input value={record?.email} disabled />
        </Form.Item>
        <Form.Item label="Phone">
          <Input value={record?.phone ?? "-"} disabled />
        </Form.Item>
        <Form.Item label="First Name" name="first_name">
          <Input />
        </Form.Item>
        <Form.Item label="Last Name" name="last_name">
          <Input />
        </Form.Item>
      </Form>
    </Edit>
  );
}
