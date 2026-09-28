export interface SidebarCategory {
	id: number;
	slug: string;
	name: string;
	count: number;
	children: SidebarCategory[];
}
