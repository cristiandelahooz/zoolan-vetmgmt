import { create } from 'zustand'

type PetFormState = {
  name: string
  type: string
  breed: string
  birthDate: string
  gender: string
  ownerId?: number
  setField: <K extends keyof PetFormState>(field: K, value: PetFormState[K]) => void
  reset: () => void
}

export const usePetFormStore = create<PetFormState>((set) => ({
  name: '',
  type: '',
  breed: '',
  birthDate: '',
  gender: '',
  ownerId: undefined,
  setField: (field, value) => set((state) => ({ ...state, [field]: value })),
  reset: () =>
    set({
      name: '',
      type: '',
      breed: '',
      birthDate: '',
      gender: '',
      ownerId: undefined,
    }),
}))
