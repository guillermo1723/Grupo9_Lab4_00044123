import { Button, Form, Input, Modal, Select, Space, Table } from 'antd'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { useState } from 'react'
import type Role from '@/models/api/entities/Role'
import type Permissions from '@/models/api/entities/Permissions'
import { queryKeys } from '@/lib/queryClient'
import { roleService, permissionService } from '@/services/api'
import { useFindAll } from '@/hooks/core/useFindAll'
import useCrud from '@/hooks/core/useCrud'

export default function RolesView() {
  const [params, setParams] = useState<Record<string, unknown>>({
    page: 0,
    size: 15,
  })

  const { data: response, isLoading } = useFindAll<Role>({
    queryKey: queryKeys.roles,
    service: roleService,
    queryParams: params,
  })

  const { data: permissionsResponse } = useFindAll<Permissions>({
    queryKey: queryKeys.permissions,
    service: permissionService,
    queryParams: { page: 0, size: 1000 },
  })

  const crud = useCrud<Role>({
    service: roleService,
    queryKey: queryKeys.roles,
  })

  const [editing, setEditing] = useState<Role | null>(null)
  const [open, setOpen] = useState(false)

  const [form] = Form.useForm()

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setParams((prev) => ({
      ...prev,
      page: (pagination.current ?? 1) - 1,
      size: pagination.pageSize ?? prev.size,
    }))
  }

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setOpen(true)
  }

  const openEdit = (role: Role) => {
    setEditing(role)
    form.setFieldsValue({
      name: role.name,
      permissions: role.permissions?.map((p: Permissions) => p.id) ?? [],
    })
    setOpen(true)
  }

  const handleOk = async () => {
    const values = await form.validateFields()
    const payload: Role = {
      name: values.name,
      permissions: (values.permissions || []).map((id: number) => ({ id })),
    }

    if (editing) {
      await crud.update({ id: editing.id!.toString(), payload })
    } else {
      await crud.create({ payload })
    }

    setOpen(false)
    form.resetFields()
  }

  const columns: ColumnsType<Role> = [
    { title: 'ID', dataIndex: 'id', key: 'id', align: 'center' },
    { title: 'Nombre', dataIndex: 'name', key: 'name', align: 'center' },
    {
      title: 'Acciones',
      key: 'actions',
      align: 'center',
      render: (_text, record) =>
        record.name !== 'ADMIN' && (
          <Space>
            <Button type="link" onClick={() => openEdit(record)}>
              Editar
            </Button>
            <Button
              type="link"
              danger
              onClick={async () =>
                await crud.update({
                  id: record.id!,
                  payload: { ...record, active: !record.active },
                })
              }
            >
              {record.active ? 'Desactivar' : 'Activar'}
            </Button>
          </Space>
        ),
    },
  ]

  const permissionsOptions = permissionsResponse?.data?.map(
    (p: Permissions) => ({
      label: p.title ?? `${p.method ?? ''} ${p.path ?? ''}`,
      value: p.id,
    })
  )

  return (
    <div>
      <div className="mb-3 flex justify-end">
        <Button type="primary" onClick={openCreate}>
          Crear rol
        </Button>
      </div>

      <Table<Role>
        columns={columns}
        dataSource={response?.data}
        loading={isLoading}
        rowKey="id"
        pagination={{
          current: (response?.pagination.page ?? 0) + 1,
          pageSize: response?.pagination.pageSize,
          total: response?.pagination.total ?? 0,
          showSizeChanger: true,
          position: ['bottomCenter'],
        }}
        onChange={handleTableChange}
      />

      <Modal
        title={editing ? 'Editar rol' : 'Crear rol'}
        open={open}
        onOk={handleOk}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Nombre" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item
            name="permissions"
            label="Permisos"
            rules={[
              {
                required: true,
                type: 'array',
                min: 1,
                message: 'Debe seleccionar al menos un permiso',
              },
            ]}
          >
            <Select mode="multiple" options={permissionsOptions} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
