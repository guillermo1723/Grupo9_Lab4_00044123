import { routesConfig } from '@/config/routes.app'
import type { RoleName } from '@/enum/role'
import type { RoutesEnum } from '@/enum/routes..app'
import type Permissions from '@/models/api/entities/Permissions'

export function hasPermission(
  permissions: Permissions[] | undefined,
  permissionKey: string
): boolean {
  if (permissionKey === '*') return true

  const [method, path] = permissionKey.split(':')
  return !!permissions?.some(
    (permission) => permission.method === method && permission.path === path
  )
}

export function isAuthorized(
  role: RoleName | undefined,
  permissions: Permissions[] | undefined,
  route: RoutesEnum
): boolean {
  const routeData = routesConfig[route]
  if (!routeData || !role) return false

  const roleAllowed = routeData.roles.includes(role) || routeData.roles.includes('*')
  if (!roleAllowed) return false

  const requiredPermissions = routeData.permission ?? ['*']
  return requiredPermissions.some((permission) =>
    hasPermission(permissions, permission)
  )
}
