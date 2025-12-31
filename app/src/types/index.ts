export type ListItem = {
  id: number;
  name: string;
  completed: boolean;
  quantity?: number;
  price?: number;
};

export type ShoppingList = {
  id: number;
  name: string;
  description?: string;
  items: ListItem[];
  completed: number;
  createdAt: Date;
};

export type User = {
  id: string;
  name: string;
  email: string;
};
