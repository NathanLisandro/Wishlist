// Tipos espelhando os DTOs da API (backend Spring Boot).

export type Pagina<T> = {
  itens: T[];
  total: number;
  page: number;
  size: number;
};

export type EventoPublico = {
  slug: string;
  nome: string;
  descricao: string | null;
  dataEvento: string | null;
  categoria: string | null;
  rsvpHabilitado: boolean;
  presenteDinheiroHabilitado: boolean;
  status: string;
  fotoUrl: string | null;
  local: string | null;
  mensagemCasal: string | null;
  mensagemFinal: string | null;
  mensagemAgradecimento: string | null;
  paletaTema: string;
  corDestaque: string | null;
  fundoUrl: string | null;
  fundoOverlay: number | null;
  estiloAnimacao: string;
};

export type RsvpAtual = {
  status: "PENDING" | "CONFIRMED" | "DECLINED" | "CANCELLED";
  acompanhantes: string[];
};

export type SessaoConvidado = {
  token: string;
  nomeConvidado: string;
  acompanhantesPermitidos: number;
  nomeEvento: string;
  slugEvento: string;
};

export type ItemDisponivel = {
  id: string;
  nome: string;
  variante: string | null;
  quantidadeTotal: number;
  vagasRestantes: number;
  linkProduto: string | null;
  imagemUrl: string | null;
  reservadoPorMim: boolean;
};

export type ItemAdmin = {
  id: string;
  nome: string;
  variante: string | null;
  quantidadeTotal: number;
  reservados: number;
  linkProduto: string | null;
  imagemUrl: string | null;
  status: string;
};

export type ConvidadoPainel = {
  id: string;
  nome: string;
  telefone: string;
  acompanhantesPermitidos: number;
  statusRsvp: string;
  acompanhantesConfirmados: number;
};

export type EventoDetalhe = {
  id: string;
  slug: string;
  nome: string;
  descricao: string | null;
  dataEvento: string | null;
  local: string | null;
  mensagemCasal: string | null;
  mensagemFinal: string | null;
  mensagemAgradecimento: string | null;
  rsvpHabilitado: boolean;
  presenteDinheiroHabilitado: boolean;
  status: string;
  fotoUrl: string | null;
  dono: boolean;
  tokenConviteGestao: string | null;
  paletaTema: string;
  corDestaque: string | null;
  fundoUrl: string | null;
  fundoOverlay: number | null;
  estiloAnimacao: string;
  modoSurpresa: boolean;
  podeGerenciarPresentes: boolean;
};

export type EventoResumo = {
  id: string;
  nome: string;
  slug: string;
  status: string;
  dataEvento: string | null;
  fotoUrl: string | null;
  dono: boolean;
};

export type Colaborador = {
  contaId: string;
  email: string;
  nomeExibicao: string;
};

export type ConvitePreview = {
  nomeEvento: string;
  slug: string;
};

export type ReservaResultado = {
  reservaId: string;
  itemId: string;
  esgotou: boolean;
};

export type MoneyGiftResultado = {
  moneyGiftId: string;
  initPoint: string;
};

export type LoginResponse = { token: string; nomeExibicao: string };

export type MeResponse = {
  id: string;
  email: string;
  nomeExibicao: string;
  onboardingConcluido: boolean;
};

export type ImportResultado = {
  importJobId: string;
  total: number;
  ok: number;
  falhas: number;
};

export type ItemExtraido = {
  nome: string;
  variante: string | null;
  quantidade: number | null;
  linkProduto: string | null;
  precoEstimado: number | null;
};

export type DashboardAcesso = {
  acao: string;
  nomeConvidado: string | null;
  ip: string | null;
  quando: string;
};

export type Dashboard = {
  nomeEvento: string;
  convidados: number;
  confirmados: number;
  recusados: number;
  acompanhantesConfirmados: number;
  totalPessoasConfirmadas: number;
  presentesReservados: number;
  presentesDinheiroAprovados: number;
  dinheiroRecebido: number;
  acessosRecentes: DashboardAcesso[];
};
