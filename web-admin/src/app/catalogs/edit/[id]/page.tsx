"use client";

import { Edit, useForm } from "@refinedev/antd";
import { Form, Input, InputNumber, Select } from "antd";

export default function CatalogEdit() {
  const { formProps, saveButtonProps } = useForm({});

  return (
    <Edit saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item label="ID" name="id">
          <Input disabled />
        </Form.Item>
        <Form.Item label="Nombre" name="title" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item label="URL de la imagen" name="image_url">
          <Input />
        </Form.Item>
        <Form.Item
          label="Duración (días)"
          name="duration_days"
          rules={[{ required: true }]}
        >
          <InputNumber min={1} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Ruta" name="route">
          <Select mode="tags" placeholder="Add destinations" />
        </Form.Item>
        <Form.Item label="Moneda" name="price_currency">
          <Input maxLength={3} placeholder="USD" />
        </Form.Item>
        <Form.Item label="Precio base" name="price_base">
          <InputNumber min={0} step={0.01} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Suplemento individual" name="price_single_supplement">
          <InputNumber min={0} step={0.01} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Impuestos" name="price_taxes">
          <InputNumber min={0} step={0.01} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Incluye" name="includes">
          <Select mode="tags" placeholder="Add included items" />
        </Form.Item>
        <Form.Item label="No incluye" name="not_included">
          <Select mode="tags" placeholder="Add excluded items" />
        </Form.Item>
        <Form.Item label="Requisitos" name="requirements">
          <Select mode="tags" placeholder="Add requirements" />
        </Form.Item>
        <Form.Item label="URL de la página" name="url">
          <Input />
        </Form.Item>
      </Form>
    </Edit>
  );
}
