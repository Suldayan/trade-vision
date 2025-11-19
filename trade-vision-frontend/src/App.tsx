import { Routes, Route, BrowserRouter as Router } from 'react-router-dom'
import './App.css'
import Home from './pages/Home'

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
      </Routes>
    </Router>
  )
}
