import { Link } from "react-router-dom";

export default function Brand({ to = "/" }) {
  return <Link to={to} className="brand" aria-label="DH Badminton Store">
    <span className="brand-mark" aria-hidden="true"><span className="brand-mark-core">DH</span><i /><b /></span>
    <span className="brand-wordmark"><strong>DH</strong> BADMINTON<small>PLAY YOUR POINT</small></span>
  </Link>;
}
