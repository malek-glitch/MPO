// Pure helpers ported from web/js/api.js, shared by the admin app and every storefront.

import type { CategoryDto, ProductDto, StoreSettingsDto } from "./types.js";

export function money(n: number | null | undefined): string {
  if (n == null) return "";
  return n.toLocaleString("fr-FR") + " TND";
}

export function qs(params: Record<string, string | number | boolean | null | undefined>): string {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;
    p.set(k, String(v));
  });
  return p.toString();
}

export interface FlatCategory {
  id: number;
  slug: string;
  name: string;
  label: string;
  depth: number;
  children: CategoryDto[];
}

export function flattenCategories(nodes: CategoryDto[], depth = 0, out: FlatCategory[] = []): FlatCategory[] {
  for (const n of nodes) {
    out.push({ id: n.id, slug: n.slug, name: n.name, label: "  ".repeat(depth) + n.name, depth, children: n.children ?? [] });
    flattenCategories(n.children ?? [], depth + 1, out);
  }
  return out;
}

export function findCategoryPath(tree: CategoryDto[], slug: string, trail: CategoryDto[] = []): CategoryDto[] | null {
  for (const n of tree) {
    const next = [...trail, n];
    if (n.slug === slug) return next;
    const found = findCategoryPath(n.children ?? [], slug, next);
    if (found) return found;
  }
  return null;
}

export const CONDITION_LABEL: Record<string, string> = {
  like_new: "Comme neuf",
  very_good: "Très bon",
  good: "Bon",
  fair: "Correct",
};

export function specLine(p: Pick<ProductDto, "processor" | "ramGb" | "storageGb" | "storageType" | "screenInches">): string {
  const parts: string[] = [];
  if (p.processor) parts.push(p.processor);
  if (p.ramGb) parts.push(p.ramGb + " Go RAM");
  if (p.storageGb) parts.push(p.storageGb + " Go " + (p.storageType || "SSD"));
  if (p.screenInches) parts.push(p.screenInches + '"');
  return parts.join(" · ");
}

export function waLink(
  store: Pick<StoreSettingsDto, "whatsappNumber" | "whatsappTemplate"> | null | undefined,
  product: (Pick<ProductDto, "brand" | "model" | "priceTnd" | "slug"> & { productUrl?: string }) | null,
): string {
  const number = store?.whatsappNumber || "";
  const template = store?.whatsappTemplate || "Bonjour, je suis intéressé(e) par : {product} ({price} DT) — {link}";
  let text: string;
  if (product) {
    const productName = `${product.brand} ${product.model}`;
    text = template
      .replaceAll("{product}", productName)
      .replaceAll("{price}", String(product.priceTnd))
      .replaceAll("{link}", product.productUrl ?? "");
  } else {
    text = "Bonjour, je suis intéressé(e) par vos ordinateurs.";
  }
  return `https://wa.me/${number}?text=${encodeURIComponent(text)}`;
}

export interface PriceInfo {
  price: number;
  compareAt: number | null;
  discountPct: number | null;
}

/** Sale pricing: a valid discount only counts when compareAtPriceTnd is set and above the current price. */
export function priceInfo(p: Pick<ProductDto, "priceTnd" | "compareAtPriceTnd">): PriceInfo {
  const compareAt = p.compareAtPriceTnd != null && p.compareAtPriceTnd > p.priceTnd ? p.compareAtPriceTnd : null;
  const discountPct = compareAt ? Math.round((1 - p.priceTnd / compareAt) * 100) : null;
  return { price: p.priceTnd, compareAt, discountPct };
}

export function escapeHtml(s: unknown): string {
  return String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]!);
}
