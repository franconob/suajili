"use client";

import { Create, useForm, useSelect } from "@refinedev/antd";
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Tabs,
} from "antd";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";

export default function TripCreate() {
  const { formProps, saveButtonProps, form, onFinish } = useForm({});

  const tripType = Form.useWatch("type", form);

  const { selectProps: userSelectProps } = useSelect({
    resource: "users",
    optionLabel: "full_name",
    optionValue: "id",
  });

  const { selectProps: catalogSelectProps } = useSelect({
    resource: "catalogs",
    optionLabel: "title",
    optionValue: "id",
  });

  const catalogTripId = Form.useWatch("catalog_trip_id", form);

  const { selectProps: departureSelectProps } = useSelect({
    resource: `catalogs/${catalogTripId}/departures`,
    optionLabel: "departure_date",
    optionValue: "id",
    queryOptions: {
      enabled: !!catalogTripId,
    },
  });

  return (
    <Create saveButtonProps={saveButtonProps} title="Crear viaje">
      <Form
        {...formProps}
        layout="vertical"
        initialValues={{ type: "group", status: "pending", currency: "USD" }}
        onFinish={(values) => {
          // eslint-disable-next-line
          const { destinations, departure_date, return_date, ...rest } = values as any;
          return onFinish({
            ...rest,
            departure_date: departure_date?.format?.("YYYY-MM-DD") ?? null,
            return_date: return_date?.format?.("YYYY-MM-DD") ?? null,
          });
        }}
      >
        <Tabs
          items={[
            {
              key: "general",
              label: "General",
              children: (
                <>
                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item
                        label="Tipo de viaje"
                        name="type"
                        rules={[{ required: true }]}
                      >
                        <Select>
                          <Select.Option value="group">Grupal</Select.Option>
                          <Select.Option value="custom">
                            Personalizado
                          </Select.Option>
                        </Select>
                      </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                      <Form.Item
                        label="Cliente"
                        name="user_id"
                        rules={[{ required: true }]}

                      >
                        <Select
                          {...userSelectProps}
                          showSearch
                          filterOption
                          placeholder="Seleccionar cliente"
                        />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item label="Estado" name="status">
                        <Select>
                          <Select.Option value="pending">
                            Pendiente
                          </Select.Option>
                          <Select.Option value="confirmed">
                            Confirmado
                          </Select.Option>
                          <Select.Option value="paid">Pagado</Select.Option>
                          <Select.Option value="completed">
                            Completado
                          </Select.Option>
                          <Select.Option value="cancelled">
                            Cancelado
                          </Select.Option>
                        </Select>
                      </Form.Item>
                    </Col>
                  </Row>

                  {tripType === "group" && (
                    <Row gutter={16}>
                      <Col xs={24} sm={12}>
                        <Form.Item
                          label="Viaje del catálogo"
                          name="catalog_trip_id"
                          rules={[{ required: true }]}
                        >
                          <Select
                            {...catalogSelectProps}
                            showSearch
                            filterOption
                            placeholder="Seleccionar viaje"
                            onChange={(value) => {
                              form.setFieldValue("catalog_trip_id", value);
                              form.setFieldValue("departure_id", undefined);
                            }}
                          />
                        </Form.Item>
                      </Col>
                      <Col xs={24} sm={12}>
                        <Form.Item label="Salida" name="departure_id">
                          <Select
                            {...departureSelectProps}
                            placeholder="Seleccionar salida"
                            disabled={!catalogTripId}
                            allowClear
                          />
                        </Form.Item>
                      </Col>
                    </Row>
                  )}

                  {tripType === "custom" && (
                    <Row gutter={16}>
                      <Col span={24}>
                        <Form.Item
                          label="Título del viaje"
                          name="title"
                          rules={[{ required: true }]}
                        >
                          <Input placeholder="Ej: Viaje a medida por Europa" />
                        </Form.Item>
                      </Col>
                    </Row>
                  )}

                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item
                        label="Fecha de salida"
                        name="departure_date"
                      >
                        <DatePicker style={{ width: "100%" }} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                      <Form.Item label="Fecha de regreso" name="return_date">
                        <DatePicker style={{ width: "100%" }} />
                      </Form.Item>
                    </Col>
                  </Row>
                </>
              ),
            },
            {
              key: "itinerary",
              label: "Itinerario",
              children:
                tripType === "group" ? (
                  <Alert
                    type="info"
                    showIcon
                    message="El itinerario se basa en el viaje del catálogo seleccionado."
                  />
                ) : (
                  <Form.List name="destinations">
                    {(fields, { add, remove }) => (
                      <>
                        {fields.map((field) => (
                          <Card
                            key={field.key}
                            size="small"
                            style={{ marginBottom: 16 }}
                            extra={
                              <Button
                                type="text"
                                danger
                                icon={<DeleteOutlined />}
                                onClick={() => remove(field.name)}
                              />
                            }
                          >
                            <Row gutter={16}>
                              <Col xs={24} sm={12}>
                                <Form.Item
                                  {...field}
                                  label="Destino"
                                  name={[field.name, "destination_name"]}
                                  rules={[{ required: true }]}
                                >
                                  <Input placeholder="Ej: París" />
                                </Form.Item>
                              </Col>
                              <Col xs={24} sm={8}>
                                <Form.Item
                                  {...field}
                                  label="País"
                                  name={[field.name, "country"]}
                                >
                                  <Input placeholder="Ej: Francia" />
                                </Form.Item>
                              </Col>
                              <Col xs={24} sm={4}>
                                <Form.Item
                                  {...field}
                                  label="Orden"
                                  name={[field.name, "sequence"]}
                                  initialValue={field.name + 1}
                                >
                                  <InputNumber
                                    min={1}
                                    style={{ width: "100%" }}
                                  />
                                </Form.Item>
                              </Col>
                            </Row>
                            <Row gutter={16}>
                              <Col xs={24} sm={8}>
                                <Form.Item
                                  {...field}
                                  label="Fecha inicio"
                                  name={[field.name, "start_date"]}
                                >
                                  <DatePicker style={{ width: "100%" }} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} sm={8}>
                                <Form.Item
                                  {...field}
                                  label="Fecha fin"
                                  name={[field.name, "end_date"]}
                                >
                                  <DatePicker style={{ width: "100%" }} />
                                </Form.Item>
                              </Col>
                              <Col xs={24} sm={8}>
                                <Form.Item
                                  {...field}
                                  label="Noches"
                                  name={[field.name, "nights"]}
                                >
                                  <InputNumber
                                    min={0}
                                    style={{ width: "100%" }}
                                  />
                                </Form.Item>
                              </Col>
                            </Row>
                            <Row gutter={16}>
                              <Col span={24}>
                                <Form.Item
                                  {...field}
                                  label="Alojamiento"
                                  name={[field.name, "accommodation"]}
                                >
                                  <Input placeholder="Ej: Hotel Le Marais 4*" />
                                </Form.Item>
                              </Col>
                            </Row>
                          </Card>
                        ))}
                        <Button
                          type="dashed"
                          onClick={() => add()}
                          block
                          icon={<PlusOutlined />}
                        >
                          Agregar destino
                        </Button>
                      </>
                    )}
                  </Form.List>
                ),
            },
            {
              key: "pricing",
              label: "Precios y notas",
              children: (
                <>
                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item label="Precio total" name="total_price">
                        <InputNumber
                          min={0}
                          step={0.01}
                          style={{ width: "100%" }}
                          placeholder="0.00"
                        />
                      </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                      <Form.Item label="Moneda" name="currency">
                        <Select>
                          <Select.Option value="USD">USD</Select.Option>
                          <Select.Option value="ARS">ARS</Select.Option>
                          <Select.Option value="EUR">EUR</Select.Option>
                        </Select>
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row gutter={16}>
                    <Col span={24}>
                      <Form.Item label="Notas" name="notes">
                        <Input.TextArea
                          rows={4}
                          placeholder="Notas adicionales sobre el viaje..."
                        />
                      </Form.Item>
                    </Col>
                  </Row>
                </>
              ),
            },
          ]}
        />
      </Form>
    </Create>
  );
}
