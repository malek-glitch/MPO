// TypeScript mirrors of api/src/main/kotlin/tn/pcecom/model/Dtos.kt — keep in sync by hand.

export interface Page<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface ErrorResponse {
  error: string;
}

// ---------- Categories ----------

export interface CategoryDto {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
  position: number;
  visible: boolean;
  children: CategoryDto[];
}

export interface CategoryInput {
  name: string;
  slug?: string | null;
  parentId?: number | null;
  position?: number;
  visible?: boolean;
}

// ---------- Products ----------

export type Condition = "like_new" | "very_good" | "good" | "fair";
export const CONDITIONS: Condition[] = ["like_new", "very_good", "good", "fair"];

export type ProductStatus = "available" | "sold" | "hidden";
export const PRODUCT_STATUSES: ProductStatus[] = ["available", "sold", "hidden"];

export interface UsedDetailsDto {
  condition: Condition;
  batteryHealth: number | null;
  defects: string | null;
  accessories: string | null;
}

export interface PhotoDto {
  id: number;
  position: number;
  thumb: string;
  medium: string;
  large: string;
}

export interface ProductDto {
  id: number;
  slug: string;
  categoryId: number;
  isUsed: boolean;
  brand: string;
  model: string;
  processor: string | null;
  ramGb: number | null;
  storageGb: number | null;
  storageType: string | null;
  gpu: string | null;
  screenInches: number | null;
  os: string | null;
  keyboard: string | null;
  priceTnd: number;
  compareAtPriceTnd: number | null;
  quantity: number;
  status: ProductStatus;
  description: string | null;
  warrantyMonths: number;
  featured: boolean;
  used: UsedDetailsDto | null;
  photos: PhotoDto[];
  createdAt: string;
}

export interface ProductInput {
  categoryId: number;
  isUsed: boolean;
  brand: string;
  model: string;
  processor?: string | null;
  ramGb?: number | null;
  storageGb?: number | null;
  storageType?: string | null;
  gpu?: string | null;
  screenInches?: number | null;
  os?: string | null;
  keyboard?: string | null;
  priceTnd: number;
  compareAtPriceTnd?: number | null;
  quantity?: number;
  status?: ProductStatus;
  description?: string | null;
  warrantyMonths?: number;
  featured?: boolean;
  used?: UsedDetailsDto | null;
}

/** Builds a valid ProductInput from an existing ProductDto, for quick-edit PUTs (promo price, featured toggle). */
export function productInputFrom(p: ProductDto): ProductInput {
  return {
    categoryId: p.categoryId,
    isUsed: p.isUsed,
    brand: p.brand,
    model: p.model,
    processor: p.processor,
    ramGb: p.ramGb,
    storageGb: p.storageGb,
    storageType: p.storageType,
    gpu: p.gpu,
    screenInches: p.screenInches,
    os: p.os,
    keyboard: p.keyboard,
    priceTnd: p.priceTnd,
    compareAtPriceTnd: p.compareAtPriceTnd,
    quantity: p.quantity,
    status: p.status,
    description: p.description,
    warrantyMonths: p.warrantyMonths,
    featured: p.featured,
    used: p.used,
  };
}

export interface StatusInput {
  status: ProductStatus;
}

export interface PhotoOrderInput {
  photoIds: number[];
}

export interface ProductFilterParams {
  category?: string;
  used?: boolean;
  condition?: Condition[];
  brand?: string[];
  ram?: number[];
  storageMin?: number;
  priceMin?: number;
  priceMax?: number;
  screenMin?: number;
  screenMax?: number;
  q?: string;
  featured?: boolean;
  status?: ProductStatus; // admin only
  includeSold?: boolean; // public only
  sort?: "newest" | "price_asc" | "price_desc";
  page?: number;
  pageSize?: number;
}

// ---------- Store settings ----------

export interface StoreSettingsDto {
  name: string;
  logoUrl: string | null;
  whatsappNumber: string;
  phone: string | null;
  address: string | null;
  facebookUrl: string | null;
  instagramUrl: string | null;
  tiktokUrl: string | null;
  whatsappTemplate: string | null;
  deliveryInfo: string | null;
}

export interface StoreSettingsInput {
  name: string;
  whatsappNumber: string;
  phone?: string | null;
  address?: string | null;
  facebookUrl?: string | null;
  instagramUrl?: string | null;
  tiktokUrl?: string | null;
  whatsappTemplate?: string | null;
  deliveryInfo?: string | null;
}

// ---------- Auth ----------

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface MeResponse {
  id: number;
  email: string;
}

// ---------- Contact clicks & stats ----------

export interface ContactClickInput {
  productId?: number | null;
}

export interface ProductClicksDto {
  productId: number;
  brand: string;
  model: string;
  clicks: number;
}

export interface DailyCountDto {
  date: string;
  count: number;
}

export interface CategoryCountDto {
  categoryId: number;
  name: string;
  count: number;
}

export interface StatsDto {
  productsAvailable: number;
  soldThisMonth: number;
  clicksLast30Days: number;
  topClicked: ProductClicksDto[];
  revenueThisMonthTnd: number;
  revenueAllTimeTnd: number;
  salesLast30Days: DailyCountDto[];
  topCategories: CategoryCountDto[];
}
