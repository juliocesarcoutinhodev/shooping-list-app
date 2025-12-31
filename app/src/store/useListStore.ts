import { create } from "zustand";
import { ListItem, ShoppingList } from "../types";

interface ListState {
  lists: ShoppingList[];
  currentList: ShoppingList | null;

  // Actions
  addList: (list: ShoppingList) => void;
  updateList: (id: number, list: Partial<ShoppingList>) => void;
  deleteList: (id: number) => void;
  setCurrentList: (list: ShoppingList | null) => void;

  // Item actions
  addItem: (listId: number, item: ListItem) => void;
  updateItem: (listId: number, itemId: number, item: Partial<ListItem>) => void;
  deleteItem: (listId: number, itemId: number) => void;
  toggleItemComplete: (listId: number, itemId: number) => void;
}

const SAMPLE_LISTS: ShoppingList[] = [
  {
    id: 1,
    name: "Produtos de Mercado",
    description: "Compras semanais no mercado",
    items: [
      { id: 1, name: "Leite", completed: true, quantity: 2, price: 4.5 },
      { id: 2, name: "Pão", completed: true, quantity: 1, price: 6.0 },
      { id: 3, name: "Ovos", completed: false, quantity: 2, price: 12.9 },
      { id: 4, name: "Manteiga", completed: false },
      { id: 5, name: "Queijo", completed: false, quantity: 1, price: 25.0 },
      { id: 6, name: "Iogurte", completed: false, quantity: 4, price: 3.5 },
      { id: 7, name: "Maçã", completed: true, quantity: 6 },
      { id: 8, name: "Banana", completed: false },
    ],
    completed: 3,
    createdAt: new Date(),
  },
  {
    id: 2,
    name: "Suprimentos Semanais",
    items: [
      { id: 1, name: "Arroz", completed: true },
      { id: 2, name: "Feijão", completed: false },
      { id: 3, name: "Macarrão", completed: false },
    ],
    completed: 1,
    createdAt: new Date(),
  },
  {
    id: 3,
    name: "Compras para Festa",
    items: [
      { id: 1, name: "Refrigerante", completed: true },
      { id: 2, name: "Cerveja", completed: true },
      { id: 3, name: "Cerveja Importada", completed: true },
      { id: 4, name: "Água", completed: true },
    ],
    completed: 4,
    createdAt: new Date(),
  },
];

export const useListStore = create<ListState>((set) => ({
  lists: SAMPLE_LISTS,
  currentList: null,

  addList: (list) =>
    set((state) => ({
      lists: [...state.lists, list],
    })),

  updateList: (id, updatedList) =>
    set((state) => ({
      lists: state.lists.map((list) =>
        list.id === id ? { ...list, ...updatedList } : list
      ),
      currentList:
        state.currentList?.id === id
          ? { ...state.currentList, ...updatedList }
          : state.currentList,
    })),

  deleteList: (id) =>
    set((state) => ({
      lists: state.lists.filter((list) => list.id !== id),
      currentList: state.currentList?.id === id ? null : state.currentList,
    })),

  setCurrentList: (list) =>
    set({
      currentList: list,
    }),

  addItem: (listId, item) =>
    set((state) => ({
      lists: state.lists.map((list) =>
        list.id === listId ? { ...list, items: [...list.items, item] } : list
      ),
      currentList:
        state.currentList?.id === listId
          ? {
              ...state.currentList,
              items: [...state.currentList.items, item],
            }
          : state.currentList,
    })),

  updateItem: (listId, itemId, updatedItem) =>
    set((state) => ({
      lists: state.lists.map((list) =>
        list.id === listId
          ? {
              ...list,
              items: list.items.map((item) =>
                item.id === itemId ? { ...item, ...updatedItem } : item
              ),
            }
          : list
      ),
      currentList:
        state.currentList?.id === listId
          ? {
              ...state.currentList,
              items: state.currentList.items.map((item) =>
                item.id === itemId ? { ...item, ...updatedItem } : item
              ),
            }
          : state.currentList,
    })),

  deleteItem: (listId, itemId) =>
    set((state) => ({
      lists: state.lists.map((list) =>
        list.id === listId
          ? {
              ...list,
              items: list.items.filter((item) => item.id !== itemId),
            }
          : list
      ),
      currentList:
        state.currentList?.id === listId
          ? {
              ...state.currentList,
              items: state.currentList.items.filter(
                (item) => item.id !== itemId
              ),
            }
          : state.currentList,
    })),

  toggleItemComplete: (listId, itemId) =>
    set((state) => ({
      lists: state.lists.map((list) =>
        list.id === listId
          ? {
              ...list,
              items: list.items.map((item) =>
                item.id === itemId
                  ? { ...item, completed: !item.completed }
                  : item
              ),
            }
          : list
      ),
      currentList:
        state.currentList?.id === listId
          ? {
              ...state.currentList,
              items: state.currentList.items.map((item) =>
                item.id === itemId
                  ? { ...item, completed: !item.completed }
                  : item
              ),
            }
          : state.currentList,
    })),
}));
