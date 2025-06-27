import React, { useState, useEffect } from 'react';
import { userApi } from '../services/userApi';
import { User } from '../types';
import UserForm from '../components/UserForm';

const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState<User | undefined>(undefined);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const usersData = await userApi.getAllUsers();
      setUsers(usersData);
    } catch (err) {
      setError('Failed to load users. Please check if the backend is running.');
      console.error('Error loading users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddUser = () => {
    setEditingUser(undefined);
    setShowForm(true);
  };

  const handleEditUser = (user: User) => {
    setEditingUser(user);
    setShowForm(true);
  };

  const handleDeleteUser = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await userApi.deleteUser(id);
        await loadUsers();
      } catch (err) {
        setError('Failed to delete user');
        console.error('Error deleting user:', err);
      }
    }
  };

  const handleFormSubmit = async (userData: Omit<User, 'id'>) => {
    try {
      if (editingUser) {
        await userApi.updateUser(editingUser.id!, userData);
      } else {
        await userApi.createUser(userData);
      }
      setShowForm(false);
      setEditingUser(undefined);
      await loadUsers();
    } catch (err) {
      setError('Failed to save user');
      console.error('Error saving user:', err);
    }
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditingUser(undefined);
  };

  if (loading) {
    return <div className="loading">Loading users...</div>;
  }

  return (
    <div className="users-page">
      <div className="users-header">
        <h1>Users</h1>
        <button onClick={handleAddUser} className="btn btn-primary">
          Add User
        </button>
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="users-table">
        {users.length === 0 ? (
          <div className="no-users">
            No users found. Add some users to get started!
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Letterboxd Username</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.letterboxdUsername || '-'}</td>
                  <td>
                    <button 
                      onClick={() => handleEditUser(user)}
                      className="btn btn-edit btn-small"
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleDeleteUser(user.id!)}
                      className="btn btn-delete btn-small"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showForm && (
        <UserForm
          user={editingUser}
          onSubmit={handleFormSubmit}
          onCancel={handleFormCancel}
        />
      )}
    </div>
  );
};

export default UsersPage;
