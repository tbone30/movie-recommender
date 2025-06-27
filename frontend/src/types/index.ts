export interface Movie {
  id?: number;
  title: string;
  genre?: string;
  director?: string;
  releaseYear?: number;
  rating?: number;
  description?: string;
}

export interface User {
  id?: number;
  username: string;
  email: string;
  letterboxdUsername?: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}
