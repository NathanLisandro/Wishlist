import type { TourStep } from "./SpotlightTour";

/** Tour do painel inicial — guia a conta nova a criar o primeiro evento. */
export const PASSOS_DASHBOARD: TourStep[] = [
  {
    title: "Bem-vindo(a)! 🎉",
    body: "Que bom ter você aqui. Em menos de 1 minuto você vai saber criar e gerenciar seu evento. Vamos lá?",
  },
  {
    target: '[data-tour="novo-evento"]',
    title: "Tudo começa aqui",
    body: "Este é o formulário para criar um evento — casamento, chá, aniversário ou o que você quiser celebrar.",
  },
  {
    target: '[data-tour="campo-nome"]',
    title: "Dê um nome",
    body: "O nome do casal ou da festa. É como seu evento vai aparecer para os convidados.",
  },
  {
    target: '[data-tour="campo-slug"]',
    title: "Seu link público",
    body: "Este é o endereço que os convidados vão acessar. Geramos um automaticamente, mas você pode personalizar.",
  },
  {
    target: '[data-tour="campo-categoria"]',
    title: "Escolha o tipo",
    body: "Selecione a categoria do evento. Isso ajuda a deixar tudo com a cara certa.",
  },
  {
    target: '[data-tour="btn-criar"]',
    title: "Criar é só um clique",
    body: "Pronto? Toque aqui para criar. Depois você adiciona presentes, convida pessoas e personaliza a página.",
  },
  {
    target: '[data-tour="lista-eventos"]',
    title: "Seus eventos aparecem aqui",
    body: "Cada evento criado vira um card nesta lista. Toque em “Gerenciar” para abrir e cuidar de tudo.",
  },
  {
    title: "É a sua vez ✨",
    body: "Crie seu primeiro evento agora. Ao abri-lo, mostramos o painel de gestão passo a passo. 💜",
  },
];

/** Tour da tela de gestão do evento — apresenta as abas e o compartilhamento. */
export const PASSOS_GERENCIAR: TourStep[] = [
  {
    title: "Evento criado! 🎉",
    body: "Este é o painel de gestão do seu evento. Deixa eu te mostrar rapidinho o que dá para fazer por aqui.",
  },
  {
    target: '[data-tour="abas"]',
    title: "Tudo organizado em abas",
    body: "Use estas abas para navegar entre as áreas do evento. Cada uma cuida de uma parte.",
  },
  {
    target: '[data-tour="aba-presentes"]',
    title: "Lista de presentes",
    body: "Em Presentes você monta sua lista: adicione itens manualmente ou importe de uma loja.",
  },
  {
    target: '[data-tour="aba-convidados"]',
    title: "Convidados e confirmações",
    body: "Em Convidados você cadastra quem vem e acompanha as confirmações de presença (RSVP).",
  },
  {
    target: '[data-tour="aba-personalizar"]',
    title: "A cara do seu evento",
    body: "Em Personalizar você muda cores, fotos e mensagens da página que os convidados veem.",
  },
  {
    target: '[data-tour="aba-pagamento"]',
    title: "Receber presentes em dinheiro",
    body: "Para receber Pix/cartão, abra a aba Pagamento e conecte sua conta Mercado Pago: cole o Access Token e a Public Key, salve, e configure a URL do webhook. O passo a passo numerado está dentro da aba.",
  },
  {
    target: '[data-tour="copiar-link"]',
    title: "Compartilhe com todos",
    body: "Quando estiver pronto, copie o link e mande para seus convidados por onde preferir.",
  },
  {
    title: "Tudo pronto! 💜",
    body: "Agora é com você. Explore as abas com calma — qualquer ajuste fica salvo na hora. Bom evento!",
  },
];
