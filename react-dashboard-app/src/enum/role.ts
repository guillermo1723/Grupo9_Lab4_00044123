export const roles = {
  admin: 'ADMIN',
  user: 'USER',
  all: '*',
} as const

export type RoleName = (typeof roles)[keyof typeof roles]
