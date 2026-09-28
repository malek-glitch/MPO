export interface Testimonial {
	name: string;
	initials: string;
	color: string;
	rating: number;
	date: string;
	text: string;
}

/**
 * Placeholder reviews styled after the store's Facebook page, shown until real ones are wired in.
 * Swap this list for the store's actual Facebook reviews before launch — never present made-up
 * reviews as genuine customer feedback.
 */
export const TESTIMONIALS: Testimonial[] = [
	{
		name: 'Amine B.',
		initials: 'AB',
		color: '#2563eb',
		rating: 5,
		date: 'Il y a 3 semaines',
		text: "Acheté un MacBook Pro d'occasion, état impeccable comme annoncé. Livraison rapide et service au top, je recommande !"
	},
	{
		name: 'Yosra K.',
		initials: 'YK',
		color: '#db2777',
		rating: 5,
		date: 'Il y a 1 mois',
		text: "Très bon rapport qualité-prix. L'équipe a répondu à toutes mes questions sur WhatsApp avant l'achat. Aucun souci depuis 6 mois."
	},
	{
		name: 'Mehdi T.',
		initials: 'MT',
		color: '#0d9488',
		rating: 5,
		date: 'Il y a 2 mois',
		text: 'Deuxième achat chez eux. PC portable gaming livré le lendemain, bien emballé. Le service après-vente répond vite.'
	},
	{
		name: 'Salma R.',
		initials: 'SR',
		color: '#7c3aed',
		rating: 4,
		date: 'Il y a 2 mois',
		text: "Bonne expérience globale, produit conforme à la description. Petit délai de livraison mais j'ai été bien informée."
	}
];
